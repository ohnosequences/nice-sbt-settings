// Nice.scalaProject

name := "nice-sbt-settings"
organization := "ohnosequences"
description := "sbt plugin accumulating some useful and nice sbt settings"

sbtPlugin := true
scalaVersion := "2.10.6"
bucketSuffix := "era7.com"

addSbtPlugin("ohnosequences"     % "sbt-s3-resolver"    % "0.14.0")  // https://github.com/ohnosequences/sbt-s3-resolver
addSbtPlugin("ohnosequences"     % "sbt-github-release" % "0.3.0")   // https://github.com/ohnosequences/sbt-github-release
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.3")   // https://github.com/sbt/sbt-release
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"       % "0.14.3")  // https://github.com/sbt/sbt-assembly
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"        % "0.1.10")  // https://github.com/rtimush/sbt-updates
addSbtPlugin("laughedelic"       % "literator"          % "0.7.0")   // https://github.com/laughedelic/literator
addSbtPlugin("com.markatta"      % "taglist-plugin"     % "1.3.1")   // https://github.com/johanandren/sbt-taglist
addSbtPlugin("org.wartremover"   % "sbt-wartremover"    % "1.0.1")   // https://github.com/puffnfresh/wartremover
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"      % "0.6.1")   // https://github.com/sbt/sbt-buildinfo

wartremoverErrors in (Compile, compile) := Seq()
// wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad)

dependencyOverrides ++= Set(
  "commons-codec"              % "commons-codec"    % "1.10",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.4",
  "org.apache.httpcomponents"  % "httpclient"       % "4.3.6",
  "com.jcraft"                 % "jsch"             % "0.1.50",
  "joda-time"                  % "joda-time"        % "2.8"
)

import ohnosequences.sbt.nice._
import sbt._, complete._, DefaultParsers._

def nextVersionParser(current: Version): Parser[Version] = {

  case class BumperParser(str: String, bumper: Version => Version) {

    def apply(ver: Version): Parser[Version] = {
      val next = bumper(ver)
      tokenDisplay(
        str ^^^ next,
        s"(${next}) ${str}"
      )
    }
  }

  val major     = BumperParser("major",     { _.bumpMajor })
  val minor     = BumperParser("minor",     { _.bumpMinor })
  val bugfix    = BumperParser("bugfix",    { _.bumpBugfix })
  val fin       = BumperParser("final",     { _.base })
  val milestone = BumperParser("milestone", { v =>  v.M(v.milestone.getOrElse(0) + 1) })
  val candidate = BumperParser("candidate", { v => v.RC(v.releaseCandidate.getOrElse(0) + 1) })

  if (current.isSnapshot) {
    failure("You cannot release a snapshot. Commit or stash the changes first.")

  } else if (current.isReleaseCandidate) {
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

commands += Command(
  "rel",
  ("release", "<tab>"),
  "Takes release type as an argument and starts release process. Available arguments are shown on tab-completion."
){ state =>

  val extracted = Project.extract(state)

  extracted.get(VersionSettings.autoImport.gitVersion) match {
    case None => failure("gitVersion doesn't have a value")
    case Some(current) => nextVersionParser(current)
  }
}{ (state, ver) =>

  state.log.info(s"Release version: [${ver}]")
  state
}
