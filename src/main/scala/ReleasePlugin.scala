/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.nice.release._

case object ReleasePlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    plugins.JvmPlugin &&
    AssemblySettings &&
    VersionSettings &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    com.markatta.sbttaglist.TagListPlugin &&
    ohnosequences.sbt.SbtGithubReleasePlugin

  val autoImport = ohnosequences.sbt.nice.release.keys
  import autoImport._

  /* ### Settings */
  override def projectConfigurations: Seq[Configuration] = Seq(Release)

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Release)(Defaults.testTasks) ++ Seq(

    keys.releaseOnlyTestTag := s"${organization.value}.test.ReleaseOnlyTest",

    testOptions in Test    += Tests.Argument("-l", keys.releaseOnlyTestTag.value),
    testOptions in Release -= Tests.Argument("-l", keys.releaseOnlyTestTag.value),
    testOptions in Release += Tests.Argument("-n", keys.releaseOnlyTestTag.value),

    sourceGenerators in Test := Def.settingDyn {
      val current = sourceGenerators.in(Test).value
      val dependsOnScalatest =
        libraryDependencies.value.exists { _.name == "scalatest" }

      if (dependsOnScalatest) Def.setting {
        current :+ tasks.generateTestTags.taskValue
      } else Def.setting {
        current
      }
    }.value,

    keys.publishFatArtifact in Release := false,

    keys.publishApiDocs := Def.inputTaskDyn {
      val arg = boolParser.parsed
      tasks.publishApiDocs(arg)
    }.evaluated,

    keys.snapshotDependencies := tasks.snapshotDependencies.value,
    keys.checkDependencies    := tasks.checkDependencies.value,

    keys.checkGit            := versionInputTask(tasks.checkGit).evaluated,
    keys.prepareReleaseNotes := versionInputTask(tasks.prepareReleaseNotes).evaluated,

    keys.prepareRelease    := versionInputTask(tasks.prepareRelease).evaluated,
    keys.makeRelease       := versionInputTask(tasks.makeRelease).evaluated,

    sbt.Keys.commands += release.commands.releaseCommand
  )

  private def boolParser: Parser[Boolean] = {
    token(Space ~> "latest" ^^^ true) ?? false
  }

  // Just a shortcut to define all input tasks that take version as an argument
  private def versionInputTask[Y](taskDef: Version => Def.Initialize[Task[Y]]):
    Def.Initialize[InputTask[Y]] = Def.inputTaskDyn {
      import parsers._

      val arg = asArgument(versionNumberParser).parsed
      taskDef(arg)
    }

}
