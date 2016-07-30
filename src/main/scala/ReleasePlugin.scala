/* ## Project release process

*/
package ohnosequences.sbt.nice

import sbt._, Keys._, complete._, DefaultParsers._

case object NewReleasePlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  // TODO: almost all other plugins:
  override def requires = empty

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
  val releaseCommandArgsParser: State => Parser[Either[String, Version]] = { state =>

    import VersionSettings.autoImport._
    import GitPlugin.autoImport._

    // Parser.failure doesn't work, so we pass error message the command action
    def fail(msg: String) = success(Left(msg))

    val extracted = Project.extract(state)
    val gitV = extracted.get(git).version()

    if (extracted.get(gitVersion) != gitV) {
      fail("gitVersion is outdated. Try to reload.")
    } else gitV match {
      case None => fail("gitVersion is unset. Check git tags and version settings.")
      case Some(ver) =>
        if (ver.isSnapshot) fail("You cannot release a snapshot. Commit or stash the changes first.")
        else nextVersionParser(ver) map Right.apply
    }
  }

  val releaseCommand = Command(
    "rel",
    ("release", "<tab>"),
    "Takes release type as an argument and starts release process. Available arguments are shown on tab-completion."
  )(releaseCommandArgsParser){ (state, parsed) =>

    parsed match {
      case Left(msg) => state.log.error(msg); state.fail
      case Right(releaseVersion) => {

        state.log.info(s"Release version: [${releaseVersion}]")
        Release.preReleaseChecks(releaseVersion)(state)
        // state
      }
    }
  }

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    commands += releaseCommand
  )

}

case object Release {

  def preReleaseChecks(releaseVersion: Version): State => State = { state =>
    checkReleaseNotes(releaseVersion)(state)
  }

  def checkReleaseNotes(releaseVersion: Version): State => State = { state =>
    val extracted = Project.extract(state)

    // TODO: these could be configurable
    val notesDir = extracted.get(baseDirectory) / "notes"
    val acceptableNames      = Set(releaseVersion.toString, "next", "unreleased", "changelog")
    val acceptableExtensions = Set("markdown", "md")

    val notesFinder: PathFinder = (notesDir * "*") filter { file =>
      (acceptableNames      contains file.base.toLowerCase) &&
      (acceptableExtensions contains file.ext)
    }

    notesFinder.get match {
      case Nil => {
        state.log.error(s"No release notes found. Acceptable names: ${acceptableNames.mkString(", ")}. Acceptable extensions: ${acceptableExtensions.mkString(", ")}.")
        state.log.error(s"Write release notes, commit and run release process again.")
        state.fail
      }

      case Seq(notesFile) => {
        val notes = IO.read(notesFile)

        if (notes.isEmpty) {
          state.log.error(s"Notes file [${notesFile}] is empty.")
          state.log.error(s"Write release notes, commit and run release process again.")
          state.fail

        } else {
          state.log.info(s"Taking release notes from the [${notesFile}] file:\n \n${notes}\n ")

          SimpleReader.readLine("Do you want to proceed with these release notes (y/n)? [y] ") match {
            case Some("n" | "N") => state.log.warn("Aborting release."); state.fail
            case _ => state // go on
          }
        }
      }

      case multipleFiles => {
        state.log.error("You have several release notes files:")
        multipleFiles.foreach { f => state.log.error(s" - ${f}") }
        state.log.error("Please, leave only one of them, commit and run release process again.")
        state.fail
      }
    }
  }
}
