/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import com.markatta.sbttaglist.TagListPlugin._

case object NewReleasePlugin extends sbt.AutoPlugin {
  import Release._

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    plugins.JvmPlugin &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    laughedelic.literator.plugin.LiteratorPlugin &&
    ohnosequences.sbt.SbtGithubReleasePlugin


  /* ### Settings */
  override def projectConfigurations: Seq[Configuration] = Seq(ReleaseTest)

  override def projectSettings: Seq[Setting[_]] =
    inConfig(ReleaseTest)(Defaults.testTasks) ++
    tagListSettings ++ Seq(

    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % Test,

    Keys.releaseOnlyTestTag := s"${organization.value}.test.ReleaseOnlyTest",

    testOptions in Test        += Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest -= Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest += Tests.Argument("-n", Keys.releaseOnlyTestTag.value),

    publishArtifact in (ReleaseTest, packageBin) := false,
    publishArtifact in (ReleaseTest, packageDoc) := false,
    publishArtifact in (ReleaseTest, packageSrc) := false,

    sourceGenerators in Test += generateTestTags.taskValue,

    Keys.publishApiDocs := publishApiDocs.value,

    Keys.snapshotDependencies := snapshotDependencies.value,
    Keys.checkDependencies := checkDependencies.value,

    Keys.checkGit          := inputTask(versionNumberArg)(checkGit).evaluated,
    Keys.checkReleaseNotes := inputTask(versionNumberArg)(checkReleaseNotes).evaluated,

    Keys.prepareRelease    := inputTask(versionNumberArg)(prepareRelease).evaluated,
    Keys.makeRelease       := inputTask(versionNumberArg)(makeRelease).evaluated,

    commands += releaseCommand
  )


  private def versionNumberArg = Def.setting {
    Space ~> versionNumberParser
  }

  private def inputTask[X, Y](parser: Def.Initialize[Parser[X]])
    (taskDef: X => Def.Initialize[Task[Y]]): Def.Initialize[InputTask[Y]] =
      Def.inputTaskDyn {
        val arg = parser.parsed
        taskDef(arg)
      }

}
