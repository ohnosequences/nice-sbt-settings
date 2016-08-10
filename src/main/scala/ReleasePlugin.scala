/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import AssemblySettings.autoImport._
import com.markatta.sbttaglist.TagListPlugin._

case object ReleasePlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    plugins.JvmPlugin &&
    AssemblySettings &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    laughedelic.literator.plugin.LiteratorPlugin &&
    ohnosequences.sbt.SbtGithubReleasePlugin

  case object autoImport {
    val Release = ohnosequences.sbt.nice.Release
    val ReleaseTest = Release.ReleaseTest
  }

  import Release._

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

    sourceGenerators in Test += generateTestTags.taskValue,

    Keys.publishFatArtifact in ReleaseTest := false,

    publish in ReleaseTest := Def.taskDyn {
      publish.value

      if (Keys.publishFatArtifact.in(ReleaseTest).value)
        Def.task { fatArtifactUpload.value }
      else
        Def.task { streams.value.log.info("Skipping fat-jar publishing.") }
    },

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
