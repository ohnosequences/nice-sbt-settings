/* ## Project version settings

   This plugin uses sbt-git to set version to <last_release_tag>-<git_describe_suffix>[-SNAPSHOT (if there are uncommited changes)].
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import scala.sys.process._
import scala.util._

case class GitRunner(wd: File) {

  private def apply(cmd: String)(args: String*): Try[String] = Try {
    sys.process.Process("git" +: cmd +: args, wd).!!.trim
  }

  def isDirty: Boolean = apply("status")(
    "--porcelain",
    "--untracked-files=no"
  ).map(_.nonEmpty).getOrElse(false)

  def describe(args: String*): Try[String] = apply("describe")(args: _*)
}

case object GitPlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = empty

  case object autoImport {

    lazy val git = settingKey[GitRunner]("Defines whether release process will wait for confirmation after each step")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    git := {
      new GitRunner(baseDirectory.value)
    }
  )
}

case object VersionSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = GitPlugin

  // val VersionRegex = """v([0-9]+.[0-9]+.[0-9]+)-?(.*)?""".r

  case object autoImport {

    lazy val gitVersion = settingKey[Option[String]]("Version based on git describe")
  }
  import autoImport._
  import GitPlugin.autoImport._

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(

    gitVersion := git.value.describe(
      // NOTE: this is a glob-pattern, not a regex
      "--match", "v[0-9]*.[0-9]*.[0-9]*",
      s"--dirty=-SNAPSHOT"
    ).toOption,

    version := gitVersion.value.getOrElse("0.0.0-SNAPSHOT")
  )

}
