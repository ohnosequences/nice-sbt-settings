## Release process 

This module defines some new release steps and defines 
a configurable sequence of the release process


```scala
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

import laughedelic.literator.plugin.LiteratorPlugin._

import com.markatta.sbttaglist._
import TagListPlugin._

import com.timushev.sbt.updates.UpdatesKeys

object ReleaseSettings extends sbt.Plugin {
```

### Setting Keys

```scala
  lazy val releaseStepByStep = settingKey[Boolean]("Defines whether release process will wait for confirmation after each step")
```

### Additional release steps
This converts a task key to an action (which is implicitly converted the to a release step)

```scala
  def releaseTask[T](key: TaskKey[T]) = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    try { 
      extracted.runAggregated(key in ref, st)
    } catch {
      case e: java.lang.Error => sys.error(e.toString)
    }
  }
```

A generic action for commiting given sequence of files with the given commit message

```scala
  // NOTE: With any VCS business we always assume Git and don't care much about other VCS systems 
  def commitFiles(msg: String, files: File*) = { st: State =>
    val extracted = Project.extract(st)
    val vcs = extracted.get(versionControlSystem).getOrElse(sys.error("No version control system is set!"))
    val base = vcs.baseDir
```

Making paths relative to the base dir

```scala
    val paths = files map { f => IO.relativize(base, f).
      getOrElse(s"Version file [${f}] is outside of this VCS repository with base directory [${base}]!")
    }
```

adding files

```scala
    vcs.cmd((Seq("add", "--all") ++ paths): _*) ! st.log
```

commiting _only_ them

```scala
    if (vcs.status.!!.trim.nonEmpty) {
      vcs.cmd((Seq("commit", "-m", msg) ++ paths): _*) ! st.log
    }
    st
  }
```

We will need to set the version temporarily during the release (and commit it later in a separate step)

```scala
  lazy val tempSetVersion = { st: State =>
    val v = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1
    st.log.info("Setting version temporarily to '" + v + "'")
    ReleaseStateTransformations.reapply(Seq(
      version in ThisBuild := v
    ), st)
  }
```

Almost the same as the standard release step, but it doesn't use our modified commitMessage task

```scala
  lazy val commitNextReleaseVersion = { st: State =>
    val extracted = Project.extract(st)
    val v = st.get(versions).
      getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._2
    commitFiles("Setting version to '" +v+ "'", extracted get versionFile)(st)
  }
```

Checks that you have written release notes in `notes/<version>.markdown` files and shows them

```scala
  lazy val checkReleaseNotes = { st: State =>
    val extracted = Project.extract(st)
    val v = extracted get (version in ThisBuild)
    val note: File = (extracted get baseDirectory) / "notes" / (v+".markdown")
    if (!note.exists || IO.read(note).isEmpty)
      sys.error(s"Aborting release. File [notes/${v}.markdown] doesn't exist or is empty. You forgot to write release notes.")
    else {
      st.log.info(s"\nTaking release notes from the [notes/${v}.markdown] file:\n \n${IO.read(note)}\n ")
      SimpleReader.readLine("Do you want to proceed with these release notes (y/n)? [y] ") match {
        case Some("n" | "N") => sys.error("Aborting release. Go write better release notes.")
        case _ => // go on
      }
      st
    }
  }
```

Almost the same as the task `dependencyUpdates`, but it outputs result as a warning 
and asks for a confirmation if needed

```scala
  lazy val checkDependecyUpdates = { st: State =>
    import com.timushev.sbt.updates.Reporter._
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    st.log.info("Checking project dependency updates...")
    val (newSt, extResolvers) = extracted.runTask(externalResolvers in ref, st)
    val data = dependencyUpdatesData(extracted.get(projectID), extracted.get(libraryDependencies), extResolvers, extracted.get(scalaVersion), extracted.get(scalaBinaryVersion))
    if (data.nonEmpty) {
      val report = dependencyUpdatesReport(extracted.get(projectID), data)
      newSt.log.warn(report)
      SimpleReader.readLine("Are you sure you want to continue with outdated dependencies (y/n)? [y] ") match {
        case Some("n" | "N") => sys.error("Aborting release due to outdated project dependencies")
        case _ => // go on
      }
    } else st.log.info("All dependencies seem to be up to date")
    newSt
  }
```

Announcing release blocks

```scala
  def shout(what: String, transit: Boolean = false) = { st: State =>
    val extracted = Project.extract(st)
    st.log.info("\n"+what+"\n")
    if (extracted.get(releaseStepByStep) && !transit) {
      SimpleReader.readLine("Do you want to continue (y/n)? [y] ") match {
        case Some("n" | "N") => sys.error("Aborting release")
        case _ => // go on
      }
    }
    st
  }
```

A release block is a sequence of release steps. We want this to be able
- to take their checks and run them first
- to operate on semantic groups of steps


```scala
  case class ReleaseBlock(name: String, steps: Seq[ReleaseStep], transit: Boolean = false)

  implicit def blockToCommand(b: ReleaseBlock) = Command.command(b.name){
    (b.steps map { s => s.check andThen s.action }).
      foldLeft(identity: State => State)(_ andThen _)
  }
```

This function takes a seuqence of release blocks and constructs a normal release process:
- it aggregates checks from all steps and puts them as a first release block
- then it runs `action` of every release step, naming release blocks and asking confirmation if needed


```scala
  def constructReleaseProcess(checks: ReleaseBlock, blocks: Seq[ReleaseBlock]): Seq[ReleaseStep] = {
    val allChecks = for( 
        block <- blocks;
        step <- block.steps
      ) yield ReleaseStep(step.check)

    val initBlock = ReleaseBlock(checks.name, checks.steps ++ allChecks, transit = true)
    val allBlocks = initBlock +: blocks
    val total = allBlocks.length

    for( 
      (block, n) <- allBlocks.zipWithIndex: Seq[(ReleaseBlock, Int)];
      heading = s"[${n+1}/${total}] ${block.name}";
      announce = ReleaseStep(shout("\n"+ heading +"\n"+ heading.replaceAll(".", "-") +"\n  ", block.transit));
      step <- announce +: block.steps
    ) yield step
  }
```

### Release settings

```scala
  lazy val releaseSettings: Seq[Setting[_]] = 
    GithubRelease.defaults ++
    ReleasePlugin.releaseSettings ++ 
    Seq(
```

We want to increment `y` in `x.y.z`

```scala
      versionBump := Version.Bump.Minor,
```

By default you want to have full controll over the release process:

```scala
      releaseStepByStep := true,

      tagComment  := {organization.value +"/"+ name.value +" v"+ (version in ThisBuild).value},
```

Adding release notes to the commit message

```scala
      commitMessage := {
        val log = streams.value.log
        val v = (version in ThisBuild).value
        val note: File = baseDirectory.value / "notes" / (v+".markdown")
        val text: String = IO.read(note)
        "Setting version to " +v+ ":\n\n"+ text
      },
```

This is a sequence of blocks (see them below)

```scala
      releaseProcess := constructReleaseProcess(
        initChecks, Seq(
        askVersionsAndCheckNotes,
        packAndTest,
        genMdDocs,
        genApiDocs,
        publishArtifacts,
        commitAndTag,
        githubRelease,
        nextVersion,
        githubPush
      ))
    )
```

### Release blocks
#### Initial checks

- check that release doesn't have snapshot or outdated dependencies
- check that we can use Github api for publishing notes
- warn if we have `TODO` or `FIXME` notes


```scala
    val initChecks = ReleaseBlock("Initial checks", Seq(
      checkSnapshotDependencies,
      checkDependecyUpdates,
      releaseTask(GithubRelease.checkGithubCredentials),
      releaseTask(TagListKeys.tagList)
    ), transit = true)
```

#### Setting release version

- inquire the current and the next release versions
- set the current one (no commiting)
- check and confirm release notes for this version


```scala
    val askVersionsAndCheckNotes = ReleaseBlock("Setting release version", Seq(
      inquireVersions.action,
      tempSetVersion,
      checkReleaseNotes
    ), transit = true)
```

#### Packaging and running tests

- try to pack
- run tests


```scala
    val packAndTest = ReleaseBlock("Packaging and running tests", Seq(
      releaseTask(Keys.`package`),
      runTest.action
    ), transit = true)
```

#### Generating markdown documentation

```scala
    val genMdDocs = ReleaseBlock("Generating markdown documentation", Seq(cleanAndGenerateDocsAction))
```

#### Generating api documentation and pushing to gh-pages

```scala
    val genApiDocs = ReleaseBlock("Generating api documentation and pushing to gh-pages", Seq(pushApiDocsToGHPagesAction))
```

#### Publishing artifacts

```scala
    val publishArtifacts = ReleaseBlock("Publishing artifacts", Seq(releaseTask(publish)))
```

#### Committing and tagging 

- commit markdown documentation
- finally set and commit release version
- make a corresponding git tag


```scala
    val commitAndTag = ReleaseBlock("Committing and tagging", Seq(
      { st: State =>
        commitFiles("Autogenerated markdown documentation", 
                    (Project.extract(st) get Literator.docsOutputDirs): _*)(st)
      },
      setReleaseVersion.action,
      commitReleaseVersion,
      tagRelease.action
    ), transit = true)
```

#### Publishing release on github 

- push tags
- publish a Github release (notes and assets)


```scala
    val githubRelease = ReleaseBlock("Publishing release on github", Seq(
      { st: State =>
        val vcs = Project.extract(st).get(versionControlSystem).
          getOrElse(sys.error("No version control system is set!"))
        vcs.cmd("push", "--tags", vcs.trackingRemote) ! st.log
        st
      },
      releaseTask(GithubRelease.releaseOnGithub)
    ))
```

#### Setting and committing next version

```scala
    val nextVersion = ReleaseBlock("Setting and committing next version", Seq(
      setNextVersion.action,
      commitNextReleaseVersion
    ))
```

#### Pushing commits to github

```scala
    val githubPush = ReleaseBlock("Pushing commits to github", Seq(
      { st: State =>
        val vcs = Project.extract(st).get(versionControlSystem).
          getOrElse(sys.error("No version control system is set!"))
        vcs.cmd("push", vcs.trackingRemote) ! st.log // pushing default branch
        vcs.cmd("push", vcs.trackingRemote, vcs.currentBranch) ! st.log // and then the current one
        st
      }
    ))

}
  

```


------

### Index

+ src
  + main
    + scala
      + [AssemblySettings.scala][main/scala/AssemblySettings.scala]
      + [DocumentationSettings.scala][main/scala/DocumentationSettings.scala]
      + [JavaSettings.scala][main/scala/JavaSettings.scala]
      + [MetadataSettings.scala][main/scala/MetadataSettings.scala]
      + [NiceProjectConfigs.scala][main/scala/NiceProjectConfigs.scala]
      + [ReleaseSettings.scala][main/scala/ReleaseSettings.scala]
      + [ResolverSettings.scala][main/scala/ResolverSettings.scala]
      + [ScalaSettings.scala][main/scala/ScalaSettings.scala]
      + [TagListSettings.scala][main/scala/TagListSettings.scala]

[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaSettings.scala]: JavaSettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/NiceProjectConfigs.scala]: NiceProjectConfigs.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md