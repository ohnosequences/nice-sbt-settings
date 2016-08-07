/* ## Project version settings

   This plugin sets version to <last_release_tag>-<git_describe_suffix>[-SNAPSHOT (if there are uncommited changes)].
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import scala.sys.process._
import scala.util._

case object VersionSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = empty

  case object autoImport {

    /* The difference between these two is that setting one will be loaded once (and may get outdated),
       while the task will be rerun on each call (and will have better logging) */
    lazy val gitVersion  = settingKey[Version]("Version based on git describe")
    lazy val gitVersionT = taskKey[Version]("Version based on git describe (as a task)")
  }
  import autoImport._

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    gitVersionT := Git(baseDirectory.value, streams.value.log).version,
    gitVersion  := Git(baseDirectory.value, sLog.value).version,
    version := gitVersion.value.toString
  )

}
