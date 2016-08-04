package ohnosequences.sbt.nice

import sbt.{ ProcessLogger => _, ProcessBuilder => _, _ }, Keys._
import scala.sys.process._
import scala.util._

case class GitRunner(
  val wd: File,
  // NOTE: this allows to pass context to the logger (like the current command failing)
  val logger: String => ProcessLogger
) {

  private def proc(subcmd: String)(args: Seq[String]): ProcessBuilder =
    sys.process.Process("git" +: subcmd +: args, wd)

  def exitCode(subcmd: String)(args: String*): Int = {
    val cmd = proc(subcmd)(args)
    cmd.!(logger(cmd.toString))
  }

  // NOTE: !! throws an exception on failure, so we wrap it with Try
  def output(subcmd: String)(args: String*): Try[String] = Try {
    val cmd = proc(subcmd)(args)
    cmd.!!(logger(cmd.toString)).trim
  }


  def isDirty: Boolean =
    output("status")(
      "--porcelain",
      "--untracked-files=no"
    ).map(_.nonEmpty).getOrElse(false)

  def tagList(pattern: String): Set[String] =
    output("tag")(
      "--list", pattern
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
      s"--match=${v.globPattern}",
      "--dirty=-SNAPSHOT",
      "--always"
    ).toOption.flatMap { str =>
      Version.parse(str).orElse {
        logger("git.describeVersion").err(s"Failed to parse a version from '${str}'")
        None
      }
    }

  // This is a fallback version of git describe for when there are no any tags yet
  // NOTE: we could use just this together with lastVersionTag for all versions (for consistency)
  private def withDescribeSuffix(ver: Version): Version = {

    val number = commitsNumber().map(_.toString)
    val hash   = output("log")("--format=g%h", "-1").toOption
    val snapsh = if (isDirty) Some("SNAPSHOT") else None

    ver( Seq(number, hash, snapsh).flatten: _* )
  }

  // This will be used for setting sbt version setting
  def version: Version = describeVersion.getOrElse {
    val ver = withDescribeSuffix(v(0,0,0))
    logger("git.version").out(s"Using fallback version: ${ver}")
    ver
  }

  // Outputs short ref name
  private def abbrevRef(ref: String): Try[String] = output("rev-parse")("--abbrev-ref", ref)

  def currentBranch:   Try[String] = abbrevRef("HEAD")
  def currentUpstream: Try[String] = abbrevRef("HEAD@{upstream}")

  // def remoteUrl(remote: String = "origin"): Option[URL] =
  //   output("remote")(
  //     "get-url",
  //     remote
  //   ).toOption.map(new URL(_))

  def remoteUrlIsReadable(remote: String = "origin", ref: String = currentBranch.getOrElse("")): Boolean =
    exitCode("ls-remote")(remote, ref) == 0
}

case object GitRunner {

  val defaultLogger: String => ProcessLogger = { ctx =>
    ProcessLogger(
      { msg => println(s"out: ${msg} (from '${ctx}')") },
      { msg => println(s"err: ${msg} (from '${ctx}')") }
    )
  }

  def apply(wd: File): GitRunner =
    GitRunner(wd, defaultLogger)

  def apply(wd: File, log: sbt.Logger): GitRunner =
    GitRunner(wd, { ctx =>
        ProcessLogger(
          msg => log.debug(s"${msg} (from ${ctx})"),
          msg =>  log.warn(s"${msg} (from ${ctx})")
        )
      }
    )

  def silent(wd: File): GitRunner =
    GitRunner(wd, _ => ProcessLogger({ _ => () }, { _ => () }))
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
