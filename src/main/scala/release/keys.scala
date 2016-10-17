package ohnosequences.sbt.nice.release

import sbt._

case object keys {

  lazy val Release = config("release").extend(Test)

  lazy val releaseOnlyTestTag = settingKey[String]("Full name of the release-only tests tag")
  lazy val publishFatArtifact = settingKey[Boolean]("Determines whether publish in release will also upload fat-jar")

  lazy val checkGit = inputKey[Unit]("Checks git repository and its remote")
  lazy val prepareReleaseNotes = inputKey[File]("Checks precense of release notes and renames the file if needed")
  lazy val snapshotDependencies = taskKey[Seq[ModuleID]]("Returns the list of dependencies with changing/snapshot versions")
  lazy val checkDependencies = taskKey[Unit]("Checks that there are no snapshot or outdated dependencies")

  lazy val publishApiDocs = inputKey[Unit]("Publishes API docs to the gh-pages branch of the repo")

  lazy val prepareRelease = inputKey[Unit]("Runs all pre-release checks sequentially")
  lazy val    makeRelease = inputKey[Unit]("Publishes the release")
}
