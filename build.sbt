Nice.scalaProject

sbtPlugin := true

name := "nice-sbt-settings"

description := "sbt plugin accumulating some useful and nice sbt settings"

organization := "ohnosequences"

bucketSuffix := "era7.com"

scalaVersion := "2.10.4"

// resolvers ++= Seq(
//   "sbt-taglist-releases" at "http://johanandren.github.com/releases/",
//   "laughedelic maven releases" at "http://dl.bintray.com/laughedelic/maven",
//   Resolver.url("laughedelic sbt-plugins", url("http://dl.bintray.com/laughedelic/sbt-plugins"))(Resolver.ivyStylePatterns)
// )

dependencyOverrides ++= Set(
  "org.apache.ivy" % "ivy" % "2.3.0",
  "commons-codec" % "commons-codec" % "1.7",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.11.0-SNAPSHOT")

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.1.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.3")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.5")

addSbtPlugin("laughedelic" % "literator-plugin" % "0.5.1")

addSbtPlugin("com.markatta" % "taglist-plugin" % "1.3")
