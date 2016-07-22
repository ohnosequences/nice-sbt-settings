package ohnosequences.sbt.nice

/* Simple generic code for working with (semantic) versions */
case class Version(
  val major: Int,
  val minor: Int,
  val bugfix: Int,
  val suffixes: Seq[String] = Seq()
) {

  val base: String = Seq(major, minor, bugfix).mkString(".")
  val suffix: String = suffixes.mkString("-")

  override def toString = base + { if (suffix.nonEmpty) "-" + suffix else "" }

  def apply(moreSuffixes: String*): Version =
    Version(major, minor, bugfix, suffixes ++ moreSuffixes)

  val isSnapshot: Boolean = suffix.endsWith("-SNAPSHOT")
  def snapshot: Version = if (isSnapshot) this else apply("SNAPSHOT")

  def bumpMajor  = Version(major + 1, 0, 0)
  def bumpMinor  = Version(major, minor + 1, 0)
  def bumpBugfix = Version(major, minor, bugfix + 1)
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
