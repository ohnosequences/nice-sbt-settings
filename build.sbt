Nice.scalaProject

sbtPlugin := true

name := "nice-sbt-settings"

description := "sbt plugin accumulating some useful and nice sbt settings"

organization := "ohnosequences"

bucketSuffix := "era7.com"

scalaVersion := "2.10.4"


addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.12.0")

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.1.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.7")

addSbtPlugin("laughedelic" % "literator-plugin" % "0.6.0")

addSbtPlugin("com.markatta" % "taglist-plugin" % "1.3.1")

addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.11")

wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad)

dependencyOverrides ++= Set(
  "commons-codec" % "commons-codec" % "1.7",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"
)
