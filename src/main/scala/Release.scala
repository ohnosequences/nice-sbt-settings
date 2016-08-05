package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import GitPlugin.autoImport._
import com.markatta.sbttaglist.TagListPlugin._
import scala.collection.immutable.SortedSet


case object Release {

  lazy val ReleaseTest = config("releaseTest").extend(Test)

  case object Keys {

    lazy val releaseOnlyTestTag = settingKey[String]("Full name of the release-only tests tag")

    lazy val checkGit = inputKey[Unit]("Checks git repository and its remote")
    lazy val checkReleaseNotes = inputKey[Either[File, File]]("Checks precense of release notes and returns its file")
    lazy val snapshotDependencies = taskKey[Seq[ModuleID]]("Returns the list of dependencies with changing/snapshot versions")
    lazy val checkDependencies = taskKey[Unit]("Checks that there are no snapshot or outdated dependencies")

    lazy val releaseChecks = inputKey[Unit]("Runs all pre-release checks sequentially")
    lazy val releasePrepare = taskKey[Unit]("Runs all pre-release checks sequentially")

    lazy val runRelease = inputKey[Unit]("Takes release type as an argument and starts release process. Available arguments are shown on tab-completion.")
  }

  type DefTask[X] = Def.Initialize[Task[X]]

  /* This class helps to create Version parser based on the string and a version transformation.
     Given a version `apply` method returns a parser which accepts `str` literal, but shows it in
     tab-completion together with the next (bumped) version.
  */
  case class BumperParser(str: String, bump: Version => Version) {

    def apply(ver: Version): Parser[Version] = {
      val next = bump(ver)
      tokenDisplay(
        str ^^^ next,
        s"(${next}) ${str}"
      )
    }
  }

  case object BumperParser {

    val major     = BumperParser("major",     { _.bumpMajor })
    val minor     = BumperParser("minor",     { _.bumpMinor })
    val bugfix    = BumperParser("bugfix",    { _.bumpBugfix })
    val milestone = BumperParser("milestone", { _.bumpMilestone })
    val candidate = BumperParser("candidate", { _.bumpCandidate })
    val fin       = BumperParser("final",     { _.base })
  }

  def versionBumperParser(current: Version): Parser[Version] = {
    import BumperParser._

    if (current.isCandidate) {
      (candidate(current) | fin(current)) ?? current.base

    } else if (current.isMilestone) {
      (milestone(current) | fin(current)) ?? current.base

    } else {
      bugfix(current) |
      (minor(current) | major(current)).flatMap { next =>
        (Space ~> (milestone(next) | candidate(next) | fin(next))) ?? next
      }
    }
  }

  /* This one just tries to parse version number from arbitrary input */
  def versionNumberParser: Parser[Version] = {
    // TODO: rewrite with proper combinators and use it in Version.parse (will give better errors than just a regex matching)
    StringBasic flatMap { str =>
      Version.parse(str) match {
        case None => failure(s"Coundn't parse version from '${str}'")
        case Some(ver) => success(ver)
      }
    }
  }

  /* Asks user for the confirmation to continue */
  private def confirmContinue(msg: String): Unit = {

    SimpleReader.readLine(s"\n${msg} [n] ") match {
      case Some("y" | "Y") => {} // go on
      case _ => sys.error("User chose to abort release process")
    }
  }

  private def announce(msg: String): DefTask[Unit] = Def.task {
    val log = streams.value.log
    log.info("")
    log.info(msg)
    log.info("")
  }


  implicit class StateOps(val state: State) extends AnyVal {

    /* A shortcut to apply settings from a command */
    def upd(settings: Setting[_]): State = {
      Project.extract(state).append(settings, state)
    }

    /* A shortcut to run a task by its key. Result is rejected. */
    def run(task: TaskKey[_]): State = {
      val (newState, _) = Project.extract(state).runTask(task, state)
      newState
    }
  }

  /* This is the action of the release command. It cannot be a task, because after release preparation we need to reload the state to update the version setting. */
  def releaseProcess(state: State, releaseVersion: Version): State = {
    val git = GitRunner(Project.extract(state).get(baseDirectory), state.log)

    state
      .upd( Keys.releasePrepare := releasePrepare(releaseVersion) )
      .run( Keys.releasePrepare )
      .upd( gitVersion := git.version )
      .run( publishLocal )
      .run( test in ReleaseTest )
  }

  /* We try to check as much as possible _before_ making any release-related changes. If these checks are not passed, it doesn't make sense to start release process at all */
  def releasePrepare(releaseVersion: Version): DefTask[Unit] = Def.sequential(
    announce("Checking git repository..."),
    checkGit(releaseVersion),
    checkGithubCredentials,

    announce("Checking code notes..."),
    checkCodeNotes,

    announce("Checking project dependencies..."),
    checkDependencies,
    sbt.Keys.update,

    announce("Running non-release tests..."),
    test in Test,

    announce("Preparing release notes and creating git tag..."),
    prepareReleaseNotesAndTag(releaseVersion)
  )


  def checkGit(releaseVersion: Version): DefTask[Unit] = Def.task {
    val log = streams.value.log
    val git = gitTask.value

    if (git.isDirty) {
      sys.error("You have uncommited changes. Commit or stash them first.")
    }

    // TODO: probably remote name should be configurable
    val remoteName = "origin"

    log.info(s"Updating remote [${remoteName}].")
    if (git.exitCode("remote")("update", remoteName) != 0) {
      sys.error(s"Remote [${remoteName}] is not set or is not accessible.")
    }

    val tagName = "v" + releaseVersion
    if (git.tagList(tagName) contains tagName) {
      sys.error(s"Git tag ${tagName} already exists. You cannot release this version.")
    }

    val current:  String = git.currentBranch.getOrElse("HEAD")
    val upstream: String = git.currentUpstream.getOrElse {
      sys.error("Couldn't get current branch upstream.")
    }
    val commitsBehind: Int = git.commitsNumber(s"${current}..${upstream}").getOrElse {
      sys.error("Couldn't compare current branch with its upstream.")
    }

    if (commitsBehind > 0) {
      sys.error(s"Local branch [${current}] is ${commitsBehind} commits behind [${upstream}]. You need to pull changes.")
    } else {
      log.info(s"Local branch [${current}] seems to be up to date with its remote upstream.")
    }
  }

  def checkCodeNotes: DefTask[Unit] = Def.task {
    // NOTE: this task outputs the list
    val list = TagListKeys.tagList.value
    if (list.flatMap{ _._2 }.nonEmpty) {
      confirmContinue("Are you sure you want to continue without fixing it (y/n)?")
    }
  }

  /* Returns the list of dependencies with changing/snapshot versions */
  def snapshotDependencies: DefTask[Seq[ModuleID]] = Def.task {

    libraryDependencies.value.filter { mod =>
      mod.isChanging ||
      mod.revision.endsWith("-SNAPSHOT")
    }
  }


  /* Almost the same as the task `dependencyUpdates`, but it outputs result as a warning
     and asks for a confirmation if needed */
  def checkDependencies: DefTask[Unit] = Def.taskDyn {
    import com.timushev.sbt.updates._, versions.{ Version => UpdVer }, UpdatesKeys._
    val log = streams.value.log

    val snapshots: Seq[ModuleID] = snapshotDependencies.value

    if (snapshots.nonEmpty) {
      log.error(s"You cannot start release process with snapshot dependencies:")
      snapshots.foreach { mod => log.error(s" - ${mod}") }
      sys.error("Update dependencies, commit and run release process again.")

    } else Def.task {

      val updatesData: Map[ModuleID, SortedSet[UpdVer]] = dependencyUpdatesData.value

      if (updatesData.nonEmpty) {
        log.warn( Reporter.dependencyUpdatesReport(projectID.value, updatesData) )
        confirmContinue("Are you sure you want to continue with outdated dependencies (y/n)?")

      } else log.info("All dependencies seem to be up to date.")
    }
  }


  /* This generates scalatest tags for marking tests (for now just release-only tests) */
  def generateTestTags: DefTask[Seq[File]] = Def.task {
    val file = (sourceManaged in Test).value / "tags.scala"

    val name = Keys.releaseOnlyTestTag.value

    IO.write(file, s"""
      |case object ${name} extends org.scalatest.Tag("${name}")
      |""".stripMargin
    )

    Seq(file)
  }


  /* This task checks the precense of release notes file and returns either
     - Left[File] if the the file needs to be renamed
     - Right[File] otherwise
  */
  def checkReleaseNotes(releaseVersion: Version): DefTask[Either[File, File]] = Def.task {
    val log = streams.value.log

    val notesDir = baseDirectory.value / "notes"

    // TODO: these could be configurable
    val alternativeNames     = Set("Changelog")
    val acceptableExtensions = Set("markdown", "md")

    val notesFinder: PathFinder = (notesDir * "*") filter { file =>
      (acceptableExtensions contains file.ext) && (
        (file.base == releaseVersion) ||
        (alternativeNames.map(_.toLowerCase) contains file.base.toLowerCase)
      )
    }

    val finalMessage = "Write release notes, commit and run release process again."

    notesFinder.get match {
      case Nil => {
        val acceptableNames = {
          alternativeNames.map(_+".md") +
          s"${releaseVersion}.markdown"
        }
        log.error(s"""No release notes found. Place them in the notes/ directory with one of the following names: ${acceptableNames.mkString("'", "', '", "'")}.""")
        sys.error(finalMessage)
      }

      case Seq(notesFile) => {
        val notes = IO.read(notesFile)
        val notesPath = notesFile.relativeTo(baseDirectory.value).getOrElse(notesFile.getPath)

        if (notes.isEmpty) {
          log.error(s"Notes file [${notesPath}] is empty.")
          sys.error(finalMessage)

        } else {
          log.info(s"Taking release notes from the [${notesPath}] file:")
          println(notes)

          confirmContinue("Do you want to proceed with these release notes (y/n)?")

          if (notesFile.base == releaseVersion) Right(notesFile)
          else Left(notesFile)
        }
      }

      case multipleFiles => {
        log.error("You have several release notes files:")
        multipleFiles.foreach { f => log.error(s" - notes/${f.name}") }
        sys.error("Please, leave only one of them, commit and run release process again.")
      }
    }
  }

  def prepareReleaseNotesAndTag(releaseVersion: Version): DefTask[Unit] = Def.task {
    val log = streams.value.log
    val git = gitTask.value

    // Either take the version-named file or rename the changelog-file and commit it
    val notesFile = checkReleaseNotes(releaseVersion).value match {
      case Right(file) => file
      case Left(changelogFile) => {
        val versionFile = baseDirectory.value / "notes" / s"${releaseVersion}.markdown"
        git.mv(changelogFile, versionFile)
        git.commit(s"Release notes for v${releaseVersion}", Set(changelogFile, versionFile))
        versionFile
      }
    }
    // TODO: (optionally) symlink notes/latest.md (useful for bintray)

    git.createTag(notesFile, releaseVersion)
    log.info(s"Created git tag [v${releaseVersion}].")
  }

}
