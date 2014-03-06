/* ## Documentation settings 

   This module takes care of producing two kinds of documentation:

   - Converting sources to markdown with [literator](https://github.com/laughedelic/literator)
   - Generating API docs (javadocs/scaladocs) and pushing it to the gh-pages branch
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import laughedelic.literator.plugin.LiteratorPlugin._

object DocumentationSettings extends sbt.Plugin {

  /* ### Settings */

  lazy val documentationSettings = 
    Literator.settings ++ Seq[Setting[_]](
      commands ++= Seq(
        cleanAndGenerateDocs,
        pushApiDocsToGHPages
      )
    )


  /* ### Commands 

     These two actions are commands instead of tasks, because they need to set other settings (i.e.
     change `State`)
  */

  lazy val cleanAndGenerateDocs = Command.command("cleanAndGenerateDocs") { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    Defaults.doClean(extracted get Literator.docsOutputDirs, Seq())
    extracted.runAggregated(Literator.generateDocs in ref, st)
  }

  def pushApiDocsToGHPages = Command.command("pushApiDocsToGHPages") { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)

    extracted get versionControlSystem match {
      case None => sys.error("No version control system is set!")
      case Some(vcs) => {
        lazy val remote: String = vcs.cmd("config", "branch.%s.remote" format vcs.currentBranch).!!.trim
        lazy val url: String = vcs.cmd("ls-remote", "--get-url", remote).!!.trim
        if (vcs.cmd("clone", "-b", "gh-pages", "--single-branch", url, "target/gh-pages").! != 0)
          sys.error("Couldn't generate API docs, because this repo doesn't have gh-pages branch")
        else {
          val ghpagesDir = extracted.get(baseDirectory) / "target" / "gh-pages"
          val newSt = ReleaseStateTransformations.reapply(Seq(
              target in (Compile, doc) := ghpagesDir / "docs" / "api" / extracted.get(version).stripSuffix("-SNAPSHOT")
            ), st)
          val lastSt = Project.extract(newSt).runAggregated(doc in Compile in ref, newSt)


          // This is a workaround to set the sorrect CWD
          // See <https://github.com/sbt/sbt-release/pull/62>
          object ghpages extends Git(ghpagesDir) {
            lazy val exec = {
              val maybeOsName = sys.props.get("os.name").map(_.toLowerCase)
              val maybeIsWindows = maybeOsName.filter(_.contains("windows"))
              maybeIsWindows.map(_ => "git.exe").getOrElse("git")
            }

            override def cmd(args: Any*): ProcessBuilder = 
              Process(exec +: args.map(_.toString), ghpagesDir)
          }
          ghpages.add("docs") ! lastSt.log
          ghpages.commit("Updated API docs for sources commit: " + vcs.currentHash) ! lastSt.log
          ghpages.cmd("push") ! lastSt.log

          lastSt
        }
      }
    }
  }

  /* ### Release steps */

  lazy val cleanAndGenerateDocsStep = ReleaseStep{ Command.process("cleanAndGenerateDocs", _: State) }
  lazy val pushApiDocsToGHPagesStep = ReleaseStep{ Command.process("pushApiDocsToGHPages", _: State) }

}
