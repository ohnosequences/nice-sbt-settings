package ohnosequences.sbt.nice

/* Simple generic code for working with (semantic) versions */
case class Version(
  val major: Int,
  val minor: Int,
  val bugfix: Int,
  val suffixes: Seq[String] = Seq()
) {

  val suffix: String = suffixes.mkString("-")

  override def toString =
    Seq(major, minor, bugfix).mkString(".") + {
      if (suffix.nonEmpty) "-" + suffix else ""
    }

  def base: Version = v(major, minor, bugfix)

  def apply(moreSuffixes: String*): Version =
    Version(major, minor, bugfix, suffixes ++ moreSuffixes)

  val isSnapshot: Boolean = suffix.endsWith("-SNAPSHOT")
  def snapshot: Version = if (isSnapshot) this else this.apply("SNAPSHOT")

  def bumpMajor:  Version = v(major + 1, 0, 0)
  def bumpMinor:  Version = v(major, minor + 1, 0)
  def bumpBugfix: Version = v(major, minor, bugfix + 1)

  // Milestone suffix:
  def M(num: Int): Version = base.apply(s"M${num}")

  val MilestoneRegex = "M([0-9]+)".r
  def milestone: Option[Int] = suffix match {
    case MilestoneRegex(num) => Some(num.toInt)
    case _ => None
  }
  def isMilestone: Boolean = milestone.nonEmpty

  // Release Candidate suffix:
  def RC(num: Int): Version = base.apply(s"RC${num}")
  
  val ReleaseCandidateRegex = "RC([0-9]+)".r
  def releaseCandidate: Option[Int] = suffix match {
    case ReleaseCandidateRegex(num) => Some(num.toInt)
    case _ => None
  }
  def isReleaseCandidate: Boolean = releaseCandidate.nonEmpty
}

// just an alias for writing v(2,1,13)("foo", "bar") or v(0,1,0).snapshot
case object v {
  def apply(x: Int, y: Int, z: Int): Version = Version(x,y,z)
}

case object Version {

  val VersionRegex = """v?([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?""".r

  def parse(str: String): Option[Version] = str match {
    case VersionRegex(maj, min, bug, suff) => Some(
      Version(
        maj.toInt,
        min.toInt,
        bug.toInt,
        suff.split('-').filter(_.nonEmpty)
      )
    )
    case _ => None
  }

}
