import ohnosequences.sbt._

Era7.allSettings


sbtPlugin := true


name := "era7-sbt-settings"

description := "sbt plugin with common settings for all era7/ohnosequences releases"

homepage := Some(url("https://github.com/ohnosequences/era7-sbt-settings"))

organization := "ohnosequences"

organizationHomepage := Some(url("http://ohnosequences.com"))

licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))


bucketSuffix := "era7.com"


scalaVersion := "2.10.3"

scalacOptions ++= Seq(
    "-feature"
  , "-language:higherKinds"
  , "-language:implicitConversions"
  , "-language:postfixOps"
  , "-deprecation"
  , "-unchecked"
  )


addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.7.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")
