/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import GitPlugin.autoImport._


case object NewReleasePlugin extends sbt.AutoPlugin {
  import Release._

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    // ohnosequences.sbt.nice.DocumentationSettings &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.ScalaSettings &&
    ohnosequences.sbt.nice.TagListSettings &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    ohnosequences.sbt.nice.GitPlugin &&
    ohnosequences.sbt.SbtGithubReleasePlugin


  private def inputTask[X, Y](parser: Def.Initialize[Parser[X]])
    (taskDef: X => Def.Initialize[Task[Y]]): Def.Initialize[InputTask[Y]] =
      Def.inputTaskDyn {
        val arg = parser.parsed
        taskDef(arg)
      }

  private def versionBumperArg = Def.setting {
    Space ~> versionBumperParser(gitVersion.value)
  }

  private def versionNumberArg = Def.setting {
    Space ~> versionNumberParser
  }


  /* ### Settings */
  override def projectConfigurations: Seq[Configuration] = Seq(ReleaseTest)

  override def projectSettings: Seq[Setting[_]] =
    inConfig(ReleaseTest)(Defaults.testTasks) ++ Seq(

    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6",

    Keys.releaseOnlyTestTag := "ReleaseOnlyTestTag",

    sourceGenerators in Test += generateTestTags.taskValue,

    testOptions in Test        += Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest -= Tests.Argument("-l", Keys.releaseOnlyTestTag.value),
    testOptions in ReleaseTest += Tests.Argument("-n", Keys.releaseOnlyTestTag.value),

    Keys.checkSnapshotDependencies := checkSnapshotDependencies.value,

    Keys.checkGit          := inputTask(versionNumberArg)(checkGit).evaluated,
    Keys.checkReleaseNotes := inputTask(versionNumberArg)(checkReleaseNotes).evaluated,
    Keys.preReleaseChecks  := inputTask(versionNumberArg)(preReleaseChecks).evaluated,

    Keys.runRelease := inputTask(versionBumperArg)(runRelease).evaluated
  )

}

case object Release {

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


  lazy val ReleaseTest = config("releaseTest").extend(Test)

  case object Keys {

    lazy val releaseOnlyTestTag = settingKey[String]("Full name of the release-only tests tag")

    lazy val checkGit = inputKey[Unit]("Checks git repository and its remote")
    lazy val checkReleaseNotes = inputKey[File]("Checks precense of release notes and returns its file")
    lazy val checkSnapshotDependencies = taskKey[Seq[ModuleID]]("Checks that project doesn't have snapshot dependencies (returns their list)")

    lazy val preReleaseChecks = inputKey[Unit]("Runs all pre-release checks sequentially")

    lazy val runRelease = inputKey[Unit]("Takes release type as an argument and starts release process. Available arguments are shown on tab-completion.")
  }

  def runRelease(releaseVersion: Version) = Def.task {
    val log = streams.value.log

    log.info(s"Release version: [${releaseVersion}]")

    preReleaseChecks(releaseVersion).value
  }

  def preReleaseChecks(releaseVersion: Version) = Def.sequential(
    checkGit(releaseVersion),
    checkGithubCredentials,
    checkCodeNotes,
    checkDependecyUpdates,
    checkSnapshotDependencies,
    checkReleaseNotes(releaseVersion),
    test in Test
  )


  lazy val checkSnapshotDependencies = Def.task {
    val log = streams.value.log

    val snapshots: Seq[ModuleID] = libraryDependencies.value.filter { mod =>
      mod.isChanging ||
      mod.revision.endsWith("-SNAPSHOT")
    }

    if (snapshots.nonEmpty) {
      log.error(s"You cannot start release process with snapshot dependencies:")
      snapshots.foreach { mod => log.error(s" - ${mod}") }
      sys.error("Update dependencies, commit and run release process again.")
    }
    snapshots
  }

  def confirmContinue(msg: String) = {

    SimpleReader.readLine(msg + " [n] ") match {
      case Some("y" | "Y") => {} // go on
      case _ => sys.error("User chose to abort release process")
    }
  }

  def checkReleaseNotes(releaseVersion: Version) = Def.task {
    val log = streams.value.log

    // TODO: these could be configurable
    val notesDir = baseDirectory.value / "notes"
    // val acceptableNames      = Set(Keys.relVersion.value.toString, "changelog")
    val acceptableNames      = Set(releaseVersion.toString, "changelog")
    val acceptableExtensions = Set("markdown", "md")

    val notesFinder: PathFinder = (notesDir * "*") filter { file =>
      (acceptableNames      contains file.base.toLowerCase) &&
      (acceptableExtensions contains file.ext)
    }

    val finalMessage = "Write release notes, commit and run release process again."

    notesFinder.get match {
      case Nil => {
        log.error("No release notes found.")
        log.error(s"Searched for ${acceptableNames.mkString("'", ".md', '", ".md'")}.")
        sys.error(finalMessage)
      }

      case Seq(notesFile) => {
        val notes = IO.read(notesFile)

        if (notes.isEmpty) {
          log.error(s"Notes file [${notesFile}] is empty.")
          sys.error(finalMessage)

        } else {
          log.info(s"Taking release notes from the [${notesFile}] file:\n ") //\n${notes}\n ")
          println(notes)

          confirmContinue("Do you want to proceed with these release notes (y/n)?")

          notesFile

          // TODO: if it's changelog.md do git mv
          // TODO: check in the end that the releaseVersion.md file is tracked by git
          // TODO: (optionally) symlink notes/latest.md (useful for bintray)
        }
      }

      case multipleFiles => {
        log.error("You have several release notes files:")
        multipleFiles.foreach { f => log.error(s" - ${notesDir.name}/${f.name}") }
        sys.error("Please, leave only one of them, commit and run release process again.")
      }
    }
  }

  /* This generates scalatest tags for marking tests (for now just release-only tests) */
  def generateTestTags: Def.Initialize[Task[Seq[File]]] = Def.task {
    val file = (sourceManaged in Test).value / "tags.scala"

    val name = Keys.releaseOnlyTestTag.value

    IO.write(file, s"""
      |case object ${name} extends org.scalatest.Tag("${name}")
      |""".stripMargin
    )

    Seq(file)
  }

  /* Almost the same as the task `dependencyUpdates`, but it outputs result as a warning
     and asks for a confirmation if needed */
  def checkDependecyUpdates = Def.task {
    import com.timushev.sbt.updates._
    val log = streams.value.log

    log.info("Checking project dependency updates...")

    val updatesData = UpdatesKeys.dependencyUpdatesData.value

    if (updatesData.nonEmpty) {
      log.warn( Reporter.dependencyUpdatesReport(projectID.value, updatesData) )
      confirmContinue("Are you sure you want to continue with outdated dependencies (y/n)?")
    } else
      log.info("All dependencies seem to be up to date.")
  }


  def checkCodeNotes = Def.task {
    import com.markatta.sbttaglist.TagListPlugin._

    // NOTE: this task outputs the list
    val list = TagListKeys.tagList.value

    if (list.flatMap{ _._2 }.nonEmpty) {
      confirmContinue("Are you sure you want to continue without fixing these notes (y/n)?")
    }
  }


  def checkGit(releaseVersion: Version) = Def.task {
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
}
