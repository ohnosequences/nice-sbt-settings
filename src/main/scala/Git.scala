package ohnosequences.sbt.nice

import sbt.{ ProcessLogger => _, ProcessBuilder => _, _ }, Keys._
import scala.sys.process._
import scala.util._

case class Git(
  val workingDir: File,
  // NOTE: this allows to pass context to the logger (the current command with its args)
  val logger: (String, Seq[String]) => ProcessLogger
) {

  def silent: Git =
    Git(workingDir, (_, _) => ProcessLogger({ _ => () }, { _ => () }))

  def cmd(subcmd: String): GitCommand = GitCommand(this)(subcmd)(Seq())
}

case class GitCommand(git: Git)(subcmd: String)(args: Seq[String]) {

  def apply(moreArgs: String*): GitCommand = GitCommand(git)(subcmd)(args ++ moreArgs)

  private def process: ProcessBuilder = sys.process.Process("git" +: subcmd +: args, git.workingDir)

  def exitCode: Int = process.!(git.logger(subcmd, args))

  // NOTE: process.!! throws an exception on failure, so we wrap it with Try
  def output: Try[String] = Try { process.!!(git.logger(subcmd, args)).trim }
}


case object Git {

  /* Some ubiquitous constants: */
  val HEAD = "HEAD"
  val origin = "origin"

  /* This constructor provides some simple console logger (independent from sbt) */
  def apply(workingDir: File): Git = Git(workingDir,
    { (cmd, args) =>
      ProcessLogger(
        msg => println(s"out: ${msg} (git ${cmd} ${args.mkString(" ")})"),
        msg => println(s"err: ${msg} (git ${cmd} ${args.mkString(" ")})")
      )
    }
  )

  /* This constructor lets you to pass sbt logger (`streams` for tasks, `sLog` for settings) */
  def apply(workingDir: File, log: sbt.Logger): Git = Git(workingDir,
    { (cmd, args) =>
      val context = s"git ${cmd} ${args.mkString(" ")}:"
      ProcessLogger(
        msg => {
          log.debug(context)
          log.debug(msg)
        },
        msg => {
          log.debug(context)
          log.warn(s"${msg} (from [git ${cmd}])")
        }
      )
    }
  )

  /* Usage in task definitions: `val git = Git.task.value` */
  def task: Def.Initialize[Task[Git]] = Def.task {
    Git(baseDirectory.value, streams.value.log)
  }

  /* The actual git API */
  implicit class GitOps(val git: Git) extends AnyVal {

    /* These are all basic subcommands used in this plugin */
    def status    = git.cmd("status")
    def tag       = git.cmd("tag")
    def log       = git.cmd("log")
    def describe  = git.cmd("describe")
    def rev_list  = git.cmd("rev-list")
    def rev_parse = git.cmd("rev-parse")
    def remote    = git.cmd("remote")
    def ls_remote = git.cmd("ls-remote")
    def clone     = git.cmd("clone")
    def reset     = git.cmd("reset")
    def add       = git.cmd("add")
    def commit    = git.cmd("commit")

    /* These are several more commands that have some restricted interface */
    def configGet(path: String*): Try[String] = git.cmd("config")(path.mkString(".")).output
    // See git help config for the meaning of different exit codes:
    def configSet(path: String*)(value: String): Int = git.cmd("config")(path.mkString("."), value).exitCode

    def mv(from: File, to: File) =
      git.cmd("mv")("-k", from.getPath, to.getPath)

    def push(remote: String)(refs: String*) =
      git.cmd("push")("--porcelain" +: remote +: refs : _*)


    /* And the rest of the commands are derived from the basic ones with certain parameters */

    /* Tells if there are any uncommitted changes */
    def isDirty: Boolean =
      status(
        "--porcelain",
        "--untracked-files=no"
      ).output.map(_.nonEmpty).getOrElse(true)

    /* Returns a set of tags matching the given (glob) pattern */
    def tagList(pattern: String): Set[String] =
      tag("--list", pattern).output
        .toOption.getOrElse("")
        .split('\n').toSet

    /* Creates an annotated tag object with notes from the given file */
    def createTag(annot: File, ver: Version): Try[String] =
      tag("--annotate", s"--file=${annot.getPath}", s"v${ver}").output

    /* Number of commits in the given range (or since the beginning) */
    def commitsNumber(range: String = HEAD): Option[Int] =
      rev_list("--count", range, "--").output.toOption.map(_.toInt)

    /* This is not used, but let it could be, see the note at `withDescribeSuffix` */
    // def lastVersionTag: Option[Version] = describe(
    //   "--match=${v.globPattern}",
    //   "--abbrev=0"
    // ).toOption.flatMap(Version.parse)

    /* git describe with version pattern tag and a snapshot suffix */
    def describeVersion: Option[Version] =
      describe(
        s"--match=${v.globPattern}",
        "--dirty=-SNAPSHOT",
        "--always"
      ).output.toOption.flatMap { str =>
        Version.parse(str).orElse {
          git.logger("git", Seq("describeVersion")).err(s"Failed to parse a version from '${str}'")
          None
        }
      }

    /* This is a fallback version of git describe for when there are no any tags yet */
    // NOTE: we could use just this together with lastVersionTag for all versions (for consistency)
    private def withDescribeSuffix(ver: Version): Version = {

      val number = commitsNumber().map(_.toString)
      val hash   = log("--format=g%h", "-1").output.toOption
      val snapsh = if (isDirty) Some("SNAPSHOT") else None

      ver( Seq(number, hash, snapsh).flatten: _* )
    }

    /* This will be used for setting sbt version setting: either git describe or the fallback version */
    def version: Version = describeVersion.getOrElse {
      val ver = withDescribeSuffix(v(0,0,0))
      git.logger("git", Seq("version")).out(s"Using fallback version: ${ver}")
      ver
    }

    /* Outputs short ref name */
    private def abbrevRef(ref: String): Try[String] = rev_parse("--abbrev-ref", ref).output

    def currentBranch:   Try[String] = abbrevRef(HEAD)
    def currentUpstream: Try[String] = abbrevRef(HEAD+"@{upstream}")

    /* Tries to look up remote name in the current branche's config */
    def currentRemote: Try[String] = currentBranch.flatMap { name => configGet("branch", name, "remote") }

    def remoteUrl(remoteName: String): Try[String] = remote("get-url", remoteName).output

    // def remoteUrlIsReadable(
    //   remote: String,
    //   ref: String = currentBranch.getOrElse("")
    // ): Boolean =
    //   ls_remote(remote, ref).exitCode == 0

    def unstage(files: File*) = reset(HEAD +: "--" +: files.map(_.getPath) : _*)
    def   stage(files: File*) =           add("--" +: files.map(_.getPath) : _*)

    /* This is more than just commit, it unstages everything that is staged now, stages only the given files and commits them (this way even files that are not in the index yet will be commited) */
    def stageAndCommit(msg: String)(files: File*) = {
      stage(files: _*)
      git.cmd("commit")(
        "--no-verify" +: // bypasses pre- and post-commit hooks
        s"--message=${msg}" +:
        "--" +: files.map(_.getPath) : _*
      )
    }
  }
}
