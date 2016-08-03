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


  def isDirty: Boolean =
    output("status")(
      "--porcelain",
      "--untracked-files=no"
    ).map(_.nonEmpty).getOrElse(false)

  def tagList(pattern: String): Set[String] =
    output("tag")(
      "--list", v.globPattern
    ).toOption.getOrElse("").split('\n').toSet

  // Number of commits in the given range (or since the beginning)
  def commitsNumber(range: String = "HEAD"): Option[Int] =
    output("rev-list")(
      "--count", range, "--"
    ).toOption.map(_.toInt)

  def describe(args: String*): Try[String] = output("describe")(args: _*)

  // def lastVersionTag: Option[Version] = describe(
  //   "--match=${v.globPattern}",
  //   "--abbrev=0"
  // ).toOption.flatMap(Version.parse)

  // describe with version pattern tag and a snapshot suffix
  def describeVersion: Option[Version] =
    describe(
      // NOTE: this is a glob-pattern, not a regex
      "--match=${v.globPattern}",
      "--dirty=-SNAPSHOT"
    ).toOption.flatMap(Version.parse)

  // This is a fallback version of git describe for when there are no any tags yet
  // NOTE: we could use just this together with lastVersionTag for all versions (for consistency)
  private def withDescribeSuffix(ver: Version): Version = {

    val n = commitsNumber().map(_.toString)
    val h = output("log")("--format=g%h", "-1").toOption
    val s = if (isDirty) Some("SNAPSHOT") else None

    ver( Seq(n,h,s).flatten: _* )
  }

  // This will be used for setting sbt version setting
  def version: Version = describeVersion getOrElse withDescribeSuffix(v(0,0,0))


  def remoteUrl(remote: String = "origin"): Option[URL] =
    output("remote")(
      "get-url",
      remote
    ).toOption.map(new URL(_))

  def remoteUrlIsReadable(remote: String = "origin"): Boolean =
    exitCode("ls-remote")(remote) == 0
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

    lazy val gitTask = taskKey[GitRunner]("Git runner instance with streams logging")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    gitTask := GitRunner(baseDirectory.value, streams.value.log)
  )
}
