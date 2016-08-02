/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._

import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._

case object NewReleasePlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires =
    // ohnosequences.sbt.nice.DocumentationSettings &&
    com.timushev.sbt.updates.UpdatesPlugin &&
    ohnosequences.sbt.nice.ScalaSettings &&
    ohnosequences.sbt.nice.TagListSettings &&
    ohnosequences.sbt.nice.WartRemoverSettings &&
    ohnosequences.sbt.SbtGithubReleasePlugin



  /* ### Settings */
  import Release._
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

    Keys.checkReleaseNotes := Def.inputTaskDyn {
      releaseArgsParser.parsed.fold(
        msg => sys.error(msg),
        ver => checkReleaseNotes(ver)
      )
    }.evaluated,

    Keys.preReleaseChecks := Def.inputTaskDyn {
      releaseArgsParser.parsed.fold(
        msg => sys.error(msg),
        ver => preReleaseChecks(ver)
      )
    }.evaluated,

    Keys.runRelease := Def.inputTaskDyn {
      releaseArgsParser.parsed.fold(
        msg => sys.error(msg),
        ver => runRelease(ver)
      )
    }.evaluated
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

  def nextVersionParser(current: Version): Parser[Version] = {
    import BumperParser._

    if (current.isCandidate) {
      (Space ~>
        (candidate(current) | fin(current))
      ) ?? current.base

    } else if (current.isMilestone) {
      (Space ~>
        (milestone(current) | fin(current))
      ) ?? current.base

    } else {
      Space ~> {
        bugfix(current) |
        (minor(current) | major(current)).flatMap { next =>
          (Space ~> (milestone(next) | candidate(next) | fin(next))) ?? next
        }
      }
    }
  }

  /* Parses arguments for the release command, but also performs additional checks  */
  val releaseArgsParser: Def.Initialize[Parser[Either[String, Version]]] = Def.setting {
    import VersionSettings.autoImport._
    import GitPlugin.autoImport._

    // NOTE: Parser.failure doesn't work, so we pass error message further to log properly
    def fail(msg: String) = token("check").map(Left.apply)

    val gitV = GitRunner.silent(baseDirectory.value).version()

    if (gitVersion.value != gitV) {
      fail("gitVersion is outdated. Try to reload.")
    } else if (gitV.isSnapshot) {
      fail("You cannot release a snapshot. Commit or stash the changes first.")
    } else {
      nextVersionParser(gitV).map(Right.apply)
    }
  }

  lazy val ReleaseTest = config("releaseTest").extend(Test)

  case object Keys {

    lazy val releaseOnlyTestTag = settingKey[String]("Full name of the release-only tests tag")

    lazy val relVersion = settingKey[Version]("Release version")

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
    checkCodeNotes,
    checkDependecyUpdates,
    checkSnapshotDependencies,
    checkGithubCredentials,
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


  def checkGit = Def.task {

    // git remote get-url
    // git ls-remote -> $! == 0
    // git remote update
    // git tag -l "releaseVersion.value" -> empty
    // git rev-list HEAD..origin/${current_branch} --count -> 0
  }
}
