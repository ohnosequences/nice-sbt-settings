/* ## Project version settings

   This plugin sets version to <last_release_tag>-<git_describe_suffix>[-SNAPSHOT (if there are uncommited changes)].
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import scala.sys.process._
import scala.util._

case object VersionSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = GitPlugin

  case object autoImport {

    lazy val gitVersion = settingKey[Version]("Version based on git describe")
  }
  import autoImport._
  import GitPlugin.autoImport._

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    gitVersion := GitRunner(baseDirectory.value).version(),
    version := gitVersion.value.toString
  )

}
