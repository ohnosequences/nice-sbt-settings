Nice.scalaProject

name := "nice-sbt-settings"
organization := "ohnosequences"
description := "sbt plugin accumulating some useful and nice sbt settings"

sbtPlugin := true
scalaVersion := "2.10.6"
bucketSuffix := "era7.com"

// https://github.com/ohnosequences/sbt-s3-resolver
addSbtPlugin("ohnosequences"     % "sbt-s3-resolver"    % "0.13.0")

// https://github.com/ohnosequences/sbt-github-release
addSbtPlugin("ohnosequences"     % "sbt-github-release" % "0.3.0")

// https://github.com/sbt/sbt-release
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.3")

// https://github.com/sbt/sbt-assembly
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"       % "0.14.2")

// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"        % "0.1.10")

// https://github.com/laughedelic/literator
addSbtPlugin("laughedelic"       % "literator"          % "0.7.0")

// https://github.com/johanandren/sbt-taglist
addSbtPlugin("com.markatta"      % "taglist-plugin"     % "1.3.1")

// https://github.com/puffnfresh/wartremover
addSbtPlugin("org.brianmckenna"  % "sbt-wartremover"    % "0.14")


wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad)

dependencyOverrides ++= Set(
  "commons-codec" % "commons-codec" % "1.10",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.4",
  "joda-time" % "joda-time" % "2.8"
)
