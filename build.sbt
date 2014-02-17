Nice.scalaProject

sbtPlugin := true

name := "nice-sbt-settings"

description := "sbt plugin accumulating some useful and nice sbt settings"

organization := "ohnosequences"

bucketSuffix := "era7.com"

dependencyOverrides += "org.apache.ivy" % "ivy" % "2.3.0"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.7.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.1")

addSbtPlugin("ohnosequences" % "literator-plugin" % "0.4.0-SNAPSHOT")
