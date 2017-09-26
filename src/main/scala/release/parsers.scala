package ohnosequences.sbt.nice.release

import sbt._, complete._, DefaultParsers._
import ohnosequences.sbt.nice._

case object parsers {

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
      (milestone(current) | candidate(current) | fin(current)) ?? current.base

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

  def asArgument[T](parser: Parser[T]): Parser[T] = { Space ~> parser }

}
