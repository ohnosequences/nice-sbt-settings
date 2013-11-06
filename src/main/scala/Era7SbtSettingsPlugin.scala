package ohnosequences.sbt

import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import ohnosequences.sbt.SbtS3Resolver._

object Era7SettingsPlugin extends sbt.Plugin {

  // Setting keys:
  lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
  lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
  lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
  lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")

  // Just some aliases for the patterns
  val mvn = Resolver.mavenStylePatterns
  val ivy = Resolver.ivyStylePatterns

  object Era7 {

    // Sets of settings:
    lazy val metainfoSettings: Seq[Setting[_]] = Seq(
        homepage := Some(url("https://github.com/"+organization.value+"/"+name.value))
      , organizationHomepage := Some(url("http://"+organization.value+".com"))
      , licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
      )

    lazy val scalaSettings: Seq[Setting[_]] = Seq(
      // this doesn't allow any conflicts in dependencies:
        conflictManager := ConflictManager.strict

      , scalaVersion := "2.10.3"
      // 2.10.x are compatible and we want to use the latest _for everything_:
      , dependencyOverrides += "org.scala-lang" % "scala-library" % "2.10.3"

      , scalacOptions ++= Seq(
            "-feature"
          , "-language:higherKinds"
          , "-language:implicitConversions"
          , "-language:postfixOps"
          , "-deprecation"
          , "-unchecked"
          )
      )

    lazy val javaSettings: Seq[Setting[_]] = Seq(
      // to omit _2.10 suffixes:
        crossPaths := false
      // to omit scala library dependency
      , autoScalaLibrary := false
      )

    lazy val resolversSettings: Seq[Setting[_]] = Seq(
        bucketSuffix := {organization.value + ".com"}
      , resolvers ++= Seq ( 
          organization.value + " public maven releases"  at 
            toHttp("s3://releases." + bucketSuffix.value)
        , organization.value + " public maven snapshots" at 
            toHttp("s3://snapshots." + bucketSuffix.value)
        // ivy
        , Resolver.url(organization.value + " public ivy releases", 
                       url(toHttp("s3://releases." + bucketSuffix.value)))(ivy)
        , Resolver.url(organization.value + " public ivy snapshots", 
                       url(toHttp("s3://snapshots." + bucketSuffix.value)))(ivy)
        ) 
      )

    lazy val publishingSettings: Seq[Setting[_]] = Seq(
        isPrivate := false
      , publishMavenStyle := true
      , publishBucketSuffix := bucketSuffix.value
      , publishS3Resolver := {
          val privacy = if (isPrivate.value) "private." else ""
          val prefix = if (isSnapshot.value) "snapshots" else "releases"
          val address = privacy+prefix+"."+publishBucketSuffix.value 
          S3Resolver( 
            name = address+" S3 publishing bucket"
          , url = "s3://"+address
          , patterns = if(publishMavenStyle.value) mvn else ivy
          , overwrite = isSnapshot.value
          )
        }
      , publishTo := {s3credentials.value map publishS3Resolver.value.toSbtResolver}
      )

    lazy val releaseSettings: Seq[Setting[_]] = 
      ReleasePlugin.releaseSettings ++ Seq(
        versionBump := Version.Bump.Minor
      , tagComment  := {name.value + " v" + (version in ThisBuild).value}
      , releaseProcess <<= thisProjectRef apply { ref =>
          Seq[ReleaseStep](
            checkSnapshotDependencies
          , inquireVersions
          , runTest
          , setReleaseVersion
          , commitReleaseVersion
          , tagRelease
          , publishArtifacts
          , setNextVersion
          , pushChanges
          )
        }
      )

    // Global combinations of settings:
    lazy val scalaProject: Seq[Setting[_]] =
      metainfoSettings ++
      scalaSettings ++
      resolversSettings ++
      publishingSettings ++
      releaseSettings

    lazy val javaProject: Seq[Setting[_]] =
      scalaProject ++
      javaSettings

  }

}
