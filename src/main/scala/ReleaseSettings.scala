/* ## Release process 

   This module defines some new release steps and defines 
   a configurable sequence of the release process
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._
import sbt.Extracted

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import DocumentationSettings._

import ohnosequences.sbt.SbtGithubReleasePlugin._

object ReleaseSettings extends sbt.Plugin {

  /* ### Setting Keys */

  lazy val releaseStepByStep = settingKey[Boolean]("Defines whether release process will wait for confirmation after each step")


  /* ### Additional release steps */

  def releaseTask[T](key: TaskKey[T]): ReleaseStep = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    try { 
      extracted.runAggregated(key in ref, st)
    } catch {
      case e: java.lang.Error => sys.error(e.toString)
    }
  }

  lazy val tempSetVersion: ReleaseStep = { st: State =>
    val v = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1
    st.log.info("Setting version temporarily to '" + v + "'")
    ReleaseStateTransformations.reapply(Seq(
      version in ThisBuild := v
    ), st)
  }

  // almost the same as the standard release step, but it doesn't use our modified commitMessage task
  lazy val commitNextReleaseVersion: ReleaseStep = { st: State =>
    val extracted = Project.extract(st)
    val v = st.get(versions).
      getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._2
    val file = Project.extract(st).get(versionFile)

    val vcs = extracted.get(versionControlSystem).getOrElse(sys.error("No version control system is set!"))
    if (vcs.status.!!.trim.nonEmpty) {
      val base = vcs.baseDir
      val relativePath = IO.relativize(base, file).
        getOrElse("Version file [%s] is outside of this VCS repository with base directory [%s]!" format(file, base))
      vcs.add(relativePath) !! st.log
      vcs.commit("Setting version to '" +v+ "'") ! st.log
    }
    st
  }

  def shout(what: String, dontStop: Boolean = false): ReleaseStep = { st: State =>
    val extracted = Project.extract(st)
    st.log.info("\n"+what+"\n")
    if (extracted.get(releaseStepByStep) && !dontStop) {
      SimpleReader.readLine("Do you want to continue (y/n)? [y] ") match {
        case Some("n" | "N") => sys.error("Aborting release")
        case _ => // go on
      }
    }
    st
  }

  /* ### Release settings */

  lazy val releaseSettings: Seq[Setting[_]] = 
    GithubRelease.defaults ++
    ReleasePlugin.releaseSettings ++ 
    Seq(
      versionBump := Version.Bump.Minor,
      releaseStepByStep := true,
      tagComment  := {organization.value +"/"+ name.value +" v"+ (version in ThisBuild).value},

      // checking release notes and adding them to the commit message
      commitMessage := {
        val log = streams.value.log
        val v = (version in ThisBuild).value
        val note: File = baseDirectory.value / "notes" / (v+".markdown")
        while (!note.exists || IO.read(note).isEmpty) {
          log.error("Release notes file "+note+"  doesn't exist or is empty!")
          SimpleReader.readLine("You can write release notes now and continue the process. Ready (y/n)? [y] ") match {
            case Some("n" | "N") => sys.error("Aborting release. No release notes.")
            case _ => // go on
          }
        }
        val text: String = IO.read(note)
        val msg = "Setting version to " +v+ ":\n\n"+ text
        log.info(msg)
        SimpleReader.readLine("Do you want to proceed with these release notes (y/n)? [y] ") match {
          case Some("n" | "N") => sys.error("Aborting release. Go write better release notes.")
          case _ => msg
        }
      },

    /* ### Release process */

      releaseProcess := Seq[ReleaseStep](

        shout("[1/10] INITIAL CHECKS", dontStop = true),
        checkSnapshotDependencies,                         // no snapshot deps in release
        releaseTask(GithubRelease.checkGithubCredentials), // check that we can publish Github release

        shout("[2/10] SETTING RELEASE VERSION", dontStop = true),
        inquireVersions,                                   // ask about release version and the next one
        tempSetVersion,                                    // set the chosed version for publishing

        shout("[3/10] PACKAGING AND RUNNING TESTS"),
        releaseTask(Keys.`package`),                       // try to package the artifacts
        runTest,                                           // compile and test

        shout("[4/10] GENERATING AND COMMITING MARKDOWN DOCUMENTATION"),
        genMarkdownDocsForRelease,                         // generate literator docs and commit if needed

        shout("[5/10] GENERATING API DOCUMENTATION AND PUSHING TO GH-PAGES"),
        genApiDocsForRelease,                              // generate javadocs or scaladocs and push it to the gh-pages branch

        shout("[6/10] PUBLISHING ARTIFACTS"),
        releaseTask(publish),                              // try to publish artifacts

        shout("[7/10] COMMITTING RELEASE VERSION AND TAGGING", dontStop = true),
        setReleaseVersion,                                 // if it was ok, set the version finally
        commitReleaseVersion,                              // and commit it
        tagRelease,                                        // and make a tag

        shout("[8/10] PUBLISHING RELEASE ON GITHUB"),
        releaseTask(GithubRelease.releaseOnGithub),        // and publish notes on github

        shout("[9/10] SETTING AND COMMITTING NEXT VERSION"),
        setNextVersion,                                    // bump the version
        commitNextReleaseVersion,                          // commit it

        shout("[10/10] PUSHING COMMITS TO GITHUB", dontStop = true),
        pushChanges                                        // and push everything to github

      )
    )
}
