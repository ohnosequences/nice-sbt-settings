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

  // describe with version pattern tag and a snapshot suffix
  def version(args: String*): Option[Version] = describe(
    Seq(
      // NOTE: this is a glob-pattern, not a regex
      "--match=v[0-9]*.[0-9]*.[0-9]*",
      "--dirty=-SNAPSHOT"
    ) ++ args : _*
  ).toOption.flatMap(Version.parse)
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
