package ohnosequences.sbt.nice

import sbt.{ ProcessLogger => _, ProcessBuilder => _, _ }, Keys._
import scala.sys.process._
import scala.util._

case class GitRunner(val wd: File, val logger: ProcessLogger) {

  private def proc(subcmd: String)(args: Seq[String]): ProcessBuilder =
    sys.process.Process("git" +: subcmd +: args, wd)

  def exitCode(subcmd: String)(args: String*): Int =
    proc(subcmd)(args).!(logger)

  // NOTE: !! throws an exception on failure, so we wrap it with Try
  def output(subcmd: String)(args: String*): Try[String] = Try {
    proc(subcmd)(args).!!(logger).trim
  }

  def isDirty: Boolean = output("status")(
    "--porcelain",
    "--untracked-files=no"
  ).map(_.nonEmpty).getOrElse(false)

  def describe(args: String*): Try[String] = output("describe")(args: _*)

  // describe with version pattern tag and a snapshot suffix
  def version(args: String*): Option[Version] = describe(
    Seq(
      // NOTE: this is a glob-pattern, not a regex
      "--match=v[0-9]*.[0-9]*.[0-9]*",
      "--dirty=-SNAPSHOT",
      "--always"
    ) ++ args : _*
  ).toOption.flatMap(Version.parse)

  def remoteUrl(remote: String = "origin"): Option[URL] = output("remote")(
    "get-url",
    remote
  ).toOption.map(new URL(_))

  def remoteUrlIsReadable(remote: String = "origin"): Boolean = {
    exitCode("ls-remote")(remote) == 0
  }
}

case object GitRunner {

  val defaultLogger = ProcessLogger(
    { msg => println("out: " + msg) },
    { msg => println("err: " + msg) }
  )

  def apply(wd: File): GitRunner =
    GitRunner(wd, defaultLogger)

  def apply(wd: File, log: sbt.Logger): GitRunner =
    GitRunner(wd, ProcessLogger(log.info(_), log.error(_)) )

  def silent(wd: File): GitRunner =
    GitRunner(wd, ProcessLogger({ _ => () }, { _ => () }))
}

case object GitPlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = empty

  case object autoImport {

    lazy val git = taskKey[GitRunner]("Git runner instance with streams logging")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    git := GitRunner(baseDirectory.value, streams.value.log)
  )
}
