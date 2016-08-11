
```scala
package ohnosequences.sbt.nice.release

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.nice._

case object parsers {
```

This class helps to create Version parser based on the string and a version transformation.
Given a version `apply` method returns a parser which accepts `str` literal, but shows it in
tab-completion together with the next (bumped) version.


```scala
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
```

This one just tries to parse version number from arbitrary input

```scala
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

```




[main/scala/AssemblySettings.scala]: ../AssemblySettings.scala.md
[main/scala/Git.scala]: ../Git.scala.md
[main/scala/JavaOnlySettings.scala]: ../JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: ../MetadataSettings.scala.md
[main/scala/package.scala]: ../package.scala.md
[main/scala/release/commands.scala]: commands.scala.md
[main/scala/release/keys.scala]: keys.scala.md
[main/scala/release/parsers.scala]: parsers.scala.md
[main/scala/release/tasks.scala]: tasks.scala.md
[main/scala/ReleasePlugin.scala]: ../ReleasePlugin.scala.md
[main/scala/ResolverSettings.scala]: ../ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ../ScalaSettings.scala.md
[main/scala/StatikaBundleSettings.scala]: ../StatikaBundleSettings.scala.md
[main/scala/Version.scala]: ../Version.scala.md
[main/scala/VersionSettings.scala]: ../VersionSettings.scala.md
[main/scala/WartRemoverSettings.scala]: ../WartRemoverSettings.scala.md