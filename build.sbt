Nice.scalaProject

name := "nice-sbt-settings"
organization := "ohnosequences"
description := "sbt plugin accumulating some useful and nice sbt settings"

sbtPlugin := true
scalaVersion := "2.10.5"
bucketSuffix := "era7.com"


addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.13.0-SNAPSHOT")
addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.2.1-SNAPSHOT")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.7")
addSbtPlugin("laughedelic" % "literator-plugin" % "0.7.0-SNAPSHOT")
addSbtPlugin("com.markatta" % "taglist-plugin" % "1.3.1")
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.11")

wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad)

dependencyOverrides ++= Set(
  "commons-codec" % "commons-codec" % "1.7",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"
)
