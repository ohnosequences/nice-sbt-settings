/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._

case object NewReleasePlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires = empty


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

    Keys.checkReleaseNotes := Def.inputTaskDyn {
      val ver = releaseArgsParser.parsed
      checkReleaseNotes(ver)
    }.evaluated,
    Keys.checkSnapshotDependencies := checkSnapshotDependencies.value,
    Keys.preReleaseChecks := Def.inputTaskDyn {
      val ver = releaseArgsParser.parsed
      preReleaseChecks(ver)
    }.evaluated,
    Keys.runRelease := Def.inputTaskDyn {
      val ver = releaseArgsParser.parsed
      runRelease(ver)
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
  val releaseArgsParser: Def.Initialize[Parser[Version]] = Def.setting {

    import VersionSettings.autoImport._
    import GitPlugin.autoImport._

    // Parser.failure doesn't work, so we pass error message the command action
    // def fail(msg: String) = any ~> success(Left(msg))

    // val extracted = Project.extract(state)
    // val gitV = extracted.get(git).version()
    val gitV = git.value.version()

    // FIXME: commented out for development, uncomment when finished
    // if (extracted.get(gitVersion) != gitV) {
    //   fail("gitVersion is outdated. Try to reload.")
    // } else
    gitV match {
      case None => failure("gitVersion is unset. Check git tags and version settings.")
      case Some(ver) =>
        // if (ver.isSnapshot) fail("You cannot release a snapshot. Commit or stash the changes first.")
        // else
        nextVersionParser(ver) //map Right.apply
    }
  }

  lazy val ReleaseTest = config("releaseTest").extend(Test)

  case object Keys {

    // lazy val releaseOnlyTestTagPackage = settingKey[String]("The package for release-only tags")
    // lazy val releaseOnlyTestTagName = settingKey[String]("Sets the name for the tag that is used to distinguish release-only tests")
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

          SimpleReader.readLine("\n Do you want to proceed with these release notes (y/n)? [y] ") match {
            case Some("n" | "N") => log.warn("Aborting release."); sys.error(finalMessage)
            case _ => notesFile // go on
          }

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

  def generateTestTags: Def.Initialize[Task[Seq[File]]] = Def.task {
    val file = (sourceManaged in Test).value / "tags.scala"

    val name = Keys.releaseOnlyTestTag.value

    IO.write(file, s"""
      |case object ${name} extends org.scalatest.Tag(${name})
      |""".stripMargin
    )

    Seq(file)
  }

}
