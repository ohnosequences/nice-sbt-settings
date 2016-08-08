package ohnosequences.sbt.nice

import sbt.{ ProcessLogger => _, ProcessBuilder => _, _ }, Keys._
import scala.sys.process._
import scala.util._

case class Git(
  val wd: File,
  // NOTE: this allows to pass context to the logger (like the current command failing)
  val logger: String => ProcessLogger
) {
  import Git._

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

  // TODO: make all these methods implicit ops

  def isDirty: Boolean =
    output("status")(
      "--porcelain",
      "--untracked-files=no"
    ).map(_.nonEmpty).getOrElse(false)

  def tagList(pattern: String): Set[String] =
    output("tag")(
      "--list", pattern
    ).toOption.getOrElse("").split('\n').toSet

  // Creates an annotated tag object with notes from the given file
  def createTag(annot: File, ver: Version) =
    output("tag")("--annotate", s"--file=${annot.getPath}", s"v${ver}")

  // Number of commits in the given range (or since the beginning)
  def commitsNumber(range: String = HEAD): Option[Int] =
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

  def currentBranch:   Try[String] = abbrevRef(HEAD)
  def currentUpstream: Try[String] = abbrevRef(HEAD+"@{upstream}")

  def remoteUrl(remote: String = origin): Option[String] =
    output("remote")(
      "get-url",
      remote
    ).toOption

  def remoteUrlIsReadable(remote: String = origin, ref: String = currentBranch.getOrElse("")): Boolean =
    exitCode("ls-remote")(remote, ref) == 0

  def mv(from: File, to: File) =
    output("mv")("-k", from.getPath, to.getPath)

  def unstage(files: File*) =
    output("reset")(HEAD +: "--" +: files.map(_.getPath) : _*)

  def stage(files: File*) =
    output("add")("--" +: files.map(_.getPath) : _*)

  /* This is more than just commit, it unstages everything that is staged now, stages only the given files and commits them (this way even files that are not in the index yet will be commited) */
  def commit(msg: String)(files: File*) = {
    unstage()
    stage(files: _*)
    output("commit")(
      "--no-verify", // bypasses pre- and post-commit hooks
      s"--message=${msg}", "--"
    )
  }

  def push(remote: String = origin)(refs: String*) =
    output("push")(remote +: refs : _*)
}

case object Git {

  // Constants:
  val HEAD = "HEAD"
  val origin = "origin"

  /* Usage in task definitions: val git = Git.task.value */
  def task: Def.Initialize[Task[Git]] = Def.task {
    Git(baseDirectory.value, streams.value.log)
  }

  val defaultLogger: String => ProcessLogger = { ctx =>
    ProcessLogger(
      msg => println(s"out: ${msg} (from ${ctx})"),
      msg => println(s"err: ${msg} (from ${ctx})")
    )
  }

  def apply(wd: File): Git =
    Git(wd, defaultLogger)

  def apply(wd: File, log: sbt.Logger): Git =
    Git(wd, { ctx =>
        ProcessLogger(
          msg => log.debug(s"${msg} (from ${ctx})"),
          msg =>  log.warn(s"${msg} (from ${ctx})")
        )
      }
    )

  def silent(wd: File): Git =
    Git(wd, _ => ProcessLogger({ _ => () }, { _ => () }))

}
