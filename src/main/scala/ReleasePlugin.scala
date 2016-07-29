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

    if (current.isSnapshot) {
      failure("You cannot release a snapshot. Commit or stash the changes first.")

    } else if (current.isCandidate) {
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

  val releaseCommand = Command(
    "rel",
    ("release", "<tab>"),
    "Takes release type as an argument and starts release process. Available arguments are shown on tab-completion."
  ){ state =>

    val extracted = Project.extract(state)

    extracted.get(VersionSettings.autoImport.gitVersion) match {
      case None => failure("gitVersion setting doesn't have a value")
      case Some(current) => nextVersionParser(current)
    }
  }{ (state, ver) =>

    state.log.info(s"Release version: [${ver}]")
    // TODO: run release process

    state
  }

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    commands += releaseCommand
  )

}
