## Documentation settings 

This module takes care of producing two kinds of documentation:

- Converting sources to markdown with [literator](https://github.com/laughedelic/literator)
- Generating API docs (javadocs/scaladocs) and pushing it to the gh-pages branch


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import laughedelic.literator.plugin.LiteratorPlugin._

object DocumentationSettings extends sbt.Plugin {
```

### Actions 

Actions (`State => State` functions) are nice because you can make from them commands and they
can be directly used as release steps


```scala
  lazy val cleanAndGenerateDocsAction = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    Defaults.doClean(extracted get Literator.docsOutputDirs, Seq())
    extracted.runAggregated(Literator.generateDocs in ref, st)
  }

  lazy val pushApiDocsToGHPagesAction = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)

    extracted get versionControlSystem match {
      case None => sys.error("No version control system is set!")
      case Some(vcs) => {
        lazy val remote: String = vcs.cmd("config", "branch.%s.remote" format vcs.currentBranch).!!.trim
        lazy val url: String = vcs.cmd("ls-remote", "--get-url", remote).!!.trim
        val ghpagesDir = IO.createTemporaryDirectory
        if (vcs.cmd("clone", "-b", "gh-pages", "--single-branch", url, ghpagesDir).! != 0)
          sys.error("Couldn't generate API docs, because this repo doesn't have gh-pages branch")
        else {
          val newSt = ReleaseStateTransformations.reapply(Seq(
              target in (Compile, doc) := ghpagesDir / "docs" / "api" / extracted.get(version).stripSuffix("-SNAPSHOT")
            ), st)
          val lastSt = Project.extract(newSt).runAggregated(doc in Compile in ref, newSt)

          // TODO: remove this once it's not needed
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
          ghpages.cmd("add", "--all", "docs/api") ! lastSt.log
          ghpages.commit("Updated API docs for sources commit: " + vcs.currentHash) ! lastSt.log
          ghpages.cmd("push") ! lastSt.log

          lastSt
        }
      }
    }
  }
```

### Commands 

Commands are added for convenience of invoking these actions manually from sbt repl


```scala
  lazy val cleanAndGenerateDocs = Command.command("cleanAndGenerateDocs")(cleanAndGenerateDocsAction)
  lazy val pushApiDocsToGHPages = Command.command("pushApiDocsToGHPages")(pushApiDocsToGHPagesAction)
```

### Settings

```scala
  lazy val documentationSettings = 
    Literator.settings ++ Seq[Setting[_]](
      commands ++= Seq(
        cleanAndGenerateDocs,
        pushApiDocsToGHPages
      )
    )

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
      + [NiceProjectConfigs.scala][main/scala/NiceProjectConfigs.scala]
      + [ReleaseSettings.scala][main/scala/ReleaseSettings.scala]
      + [ResolverSettings.scala][main/scala/ResolverSettings.scala]
      + [ScalaSettings.scala][main/scala/ScalaSettings.scala]
      + [TagListSettings.scala][main/scala/TagListSettings.scala]

[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaSettings.scala]: JavaSettings.scala.md
[main/scala/NiceProjectConfigs.scala]: NiceProjectConfigs.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md