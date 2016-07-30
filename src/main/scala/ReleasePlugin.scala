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
      case Right(ver) => {

        state.log.info(s"Release version: [${ver}]")
        // TODO: run release process
        state
      }
    }
  }

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    commands += releaseCommand
  )

}
