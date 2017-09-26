package ohnosequences.sbt.nice

/* Simple generic code for working with (semantic) versions */
case class Version(
  val major: Int,
  val minor: Int,
  val bugfix: Int,
  val suffixes: Seq[String]
) {

  val suffix: String = suffixes.mkString("-")

  override def toString =
    Seq(major, minor, bugfix).mkString(".") + {
      if (suffix.nonEmpty) "-" + suffix else ""
    }

  def base: Version = v(major, minor, bugfix)

  // def apply(moreSuffixes: Seq[String]): Version =
  //   Version(major, minor, bugfix, suffixes ++ moreSuffixes)

  def apply(moreSuffixes: String*): Version =
    Version(major, minor, bugfix, suffixes ++ moreSuffixes)

  val isSnapshot: Boolean = suffix.endsWith("-SNAPSHOT")
  def snapshot: Version = if (isSnapshot) this else this.apply("SNAPSHOT")

  // Milestone suffix:
  def M(num: Int): Version = base.apply(s"M${num}")

  lazy val milestone: Option[Int] = suffixes.headOption.flatMap {
    case v.regex.milestone(num) => Some(num.toInt)
    case _ => None
  }
  lazy val isMilestone: Boolean = milestone.nonEmpty

  // Release Candidate suffix:
  def RC(num: Int): Version = base.apply(s"RC${num}")

  lazy val candidate: Option[Int] = suffixes.headOption.flatMap {
    case v.regex.candidate(num) => Some(num.toInt)
    case _ => None
  }
  lazy val isCandidate: Boolean = candidate.nonEmpty

  // bumping:
  def bumpMajor:  Version = v(major + 1, 0, 0)
  def bumpMinor:  Version = v(major, minor + 1, 0)
  def bumpBugfix: Version = v(major, minor, bugfix + 1)

  def bumpMilestone: Version =  this.M(milestone.getOrElse(0) + 1)
  def bumpCandidate: Version = this.RC(candidate.getOrElse(0) + 1)
}

case object v {
  // just an alias for writing v(2,1,13)("foo", "bar") or v(0,1,0).snapshot
  def apply(x: Int, y: Int, z: Int): Version = Version(x,y,z, Seq())

  // NOTE: this is a glob-pattern for git, not a regex
  val globPattern = "v[0-9]*.[0-9]*.[0-9]*"

  case object regex {
    val version = """v?([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?""".r

    val milestone = "M([0-9]+)".r
    val candidate = "RC([0-9]+)".r
  }
}

case object Version {

  def parse(str: String): Option[Version] = str match {
    case v.regex.version(maj, min, bug, suff) => Some(
      Version(
        maj.toInt,
        min.toInt,
        bug.toInt,
        Option(suff).getOrElse("").split('-').filter(_.nonEmpty)
      )
    )
    case _ => None
  }

}
