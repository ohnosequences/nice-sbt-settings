
```scala
package ohnosequences.sbt.nice.release

import sbt._

case object keys {

  lazy val Release = config("release").extend(Test)

  lazy val releaseOnlyTestTag = settingKey[String]("Full name of the release-only tests tag")
  lazy val publishFatArtifact = settingKey[Boolean]("Determines whether publish in release will also upload fat-jar")

  lazy val checkGit = inputKey[Unit]("Checks git repository and its remote")
  lazy val checkReleaseNotes = inputKey[Either[File, File]]("Checks precense of release notes and returns its file")
  lazy val snapshotDependencies = taskKey[Seq[ModuleID]]("Returns the list of dependencies with changing/snapshot versions")
  lazy val checkDependencies = taskKey[Unit]("Checks that there are no snapshot or outdated dependencies")

  lazy val publishLiteratorDocs = taskKey[Unit]("Generates, commits and pushes Literator source docs")
  lazy val publishApiDocs = inputKey[Unit]("Publishes API docs to the gh-pages branch of the repo")

  lazy val prepareRelease = inputKey[Unit]("Runs all pre-release checks sequentially")
  lazy val    makeRelease = inputKey[Unit]("Publishes the release")
}

```




[main/scala/AssemblySettings.scala]: ../AssemblySettings.scala.md
[main/scala/Git.scala]: ../Git.scala.md
[main/scala/JavaOnlySettings.scala]: ../JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: ../MetadataSettings.scala.md
[main/scala/package.scala]: ../package.scala.md
[main/scala/release/commands.scala]: commands.scala.md
[main/scala/release/keys.scala]: keys.scala.md
[main/scala/release/parsers.scala]: parsers.scala.md
[main/scala/release/tasks.scala]: tasks.scala.md
[main/scala/ReleasePlugin.scala]: ../ReleasePlugin.scala.md
[main/scala/ResolverSettings.scala]: ../ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ../ScalaSettings.scala.md
[main/scala/StatikaBundleSettings.scala]: ../StatikaBundleSettings.scala.md
[main/scala/Version.scala]: ../Version.scala.md
[main/scala/VersionSettings.scala]: ../VersionSettings.scala.md
[main/scala/WartRemoverSettings.scala]: ../WartRemoverSettings.scala.md