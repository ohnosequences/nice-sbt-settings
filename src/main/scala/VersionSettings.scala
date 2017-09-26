/* ## Project version settings

   This plugin sets version to <last_release_tag>-<git_describe_suffix>[-SNAPSHOT (if there are uncommited changes)].
*/
package ohnosequences.sbt.nice

import sbt._, Keys._

case object VersionSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = plugins.IvyPlugin

  case object autoImport {

    /* The difference between these two is that setting one will be loaded once (and may get outdated),
       while the task will be rerun on each call (and will have better logging) */
    lazy val gitVersion  = settingKey[Version]("Version based on git describe")
    lazy val gitVersionT = taskKey[Version]("Version based on git describe (as a task)")
    lazy val publishCarefully = taskKey[Unit]("Checks versions before publishing")
  }
  import autoImport._

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    gitVersionT := Git(baseDirectory.value, streams.value.log).version,
    gitVersion  := {
      // NOTE: we can't place it in version setting because it will be overriden by the one defined in version.sbt
      if ((baseDirectory.value / "version.sbt").exists)
        sLog.value.warn("You should remove [version.sbt] file to use Git-based version management")

      Git(baseDirectory.value, sLog.value).version
    },
    version := gitVersion.value.toString,

    publishCarefully := publishCarefullyDef.value,
    publish          := publishCarefullyDef.value
  )

  /* This is a replacement for publish, that warns you if the git repo is dirty or the version is outdated */
  def publishCarefullyDef: DefTask[Unit] = Def.taskDyn {
    val log = streams.value.log
    val git = Git(baseDirectory.value, log)

    val loaded = gitVersion.value
    val actual = git.version

    if (git.hasChanges) Def.task {
      log.error("You have uncommited changes. Commit or stash them and reload.")
      log.error("If you want to publish a snapshot, use publishLocal. But then don't forget to clean ivy cache.")
      sys.error("Git repository is not clean.")

    } else if (loaded != actual) Def.task {
      log.error(s"Current version ${loaded} is outdated (should be ${actual}). Try to reload.")
      sys.error("Outdated version setting.")

    } else
      // NOTE: we avoid refferring to publish directly, so this is how it's defined in sbt:
      Classpaths.publishTask(Keys.publishConfiguration, Keys.deliver)
      // Def.task { publish.value }
  }
}
