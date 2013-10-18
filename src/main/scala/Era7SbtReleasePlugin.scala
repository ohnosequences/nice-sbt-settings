package ohnosequences.sbt

import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import ohnosequences.sbt.SbtS3Resolver._

object Era7SbtRelease extends sbt.Plugin {

  // Setting keys:
  lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
  lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
  lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
  lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")

  // Just some aliases for the patterns
  val mvn = Resolver.mavenStylePatterns
  val ivy = Resolver.ivyStylePatterns

  // Sets of settings:
  object Era7 {

    lazy val allSettings =
      resolversSettings ++
      publishingSettings ++
      releaseSettings

    lazy val resolversSettings = Seq(
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

    lazy val publishingSettings = Seq(
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

    lazy val releaseSettings = ReleasePlugin.releaseSettings ++ Seq(
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
  }
}
