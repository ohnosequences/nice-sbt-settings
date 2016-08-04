/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import GitPlugin.autoImport._
import com.markatta.sbttaglist.TagListPlugin._

case object NewReleasePlugin extends sbt.AutoPlugin {
  import Release._

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    // ohnosequences.sbt.nice.DocumentationSettings &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.ScalaSettings &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    ohnosequences.sbt.nice.GitPlugin &&
    ohnosequences.sbt.SbtGithubReleasePlugin


  private def inputTask[X, Y](parser: Def.Initialize[Parser[X]])
    (taskDef: X => Def.Initialize[Task[Y]]): Def.Initialize[InputTask[Y]] =
      Def.inputTaskDyn {
        val arg = parser.parsed
        taskDef(arg)
      }

  private def versionBumperArg = Def.setting {
    Space ~> versionBumperParser(gitVersion.value)
  }

  private def versionNumberArg = Def.setting {
    Space ~> versionNumberParser
  }


  /* ### Settings */
  override def projectConfigurations: Seq[Configuration] = Seq(ReleaseTest)

  override def projectSettings: Seq[Setting[_]] =
    inConfig(ReleaseTest)(Defaults.testTasks) ++
    tagListSettings ++ Seq(

    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6",

    Keys.releaseOnlyTestTag := "ReleaseOnlyTestTag",

    sourceGenerators in Test += generateTestTags.taskValue,

    testOptions in Test        += Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest -= Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest += Tests.Argument("-n", Keys.releaseOnlyTestTag.value),


    Keys.checkDependencies := checkDependencies.value,

    Keys.checkGit          := inputTask(versionNumberArg)(checkGit).evaluated,
    Keys.checkReleaseNotes := inputTask(versionNumberArg)(checkReleaseNotes).evaluated,
    Keys.releasePrepare    := inputTask(versionNumberArg)(releasePrepare).evaluated,

    // Keys.runRelease := inputTask(versionBumperArg)(runRelease).evaluated
    commands += Command("rel")(_ => versionBumperArg.value)(releaseProcess)
  )

}
