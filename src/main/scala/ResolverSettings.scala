/* ## Resolver-related settings

   This module defines resolvers for library dependencies and publishing
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import ohnosequences.sbt.SbtS3Resolver._

object ResolverSettings extends sbt.Plugin {

  /* ### Setting keys */

  lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
  lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
  lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
  lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")

  /* ### Settings */

  // Just some aliases for the patterns
  val mvn = Resolver.mavenStylePatterns
  val ivy = Resolver.ivyStylePatterns

  lazy val resolverSettings: Seq[Setting[_]] = 
    S3Resolver.defaults ++ 
    Seq(

      /* Adding default maven/ivy resolvers with the default `bucketSuffix` */
      bucketSuffix := organization.value + ".com",
      resolvers ++= Seq ( 
        organization.value + " public maven releases"  at s3("releases." + bucketSuffix.value).toHttp,
        organization.value + " public maven snapshots" at s3("snapshots." + bucketSuffix.value).toHttp,
        Resolver.url(organization.value + " public ivy releases", url(s3("releases." + bucketSuffix.value).toHttp))(ivy),
        Resolver.url(organization.value + " public ivy snapshots", url(s3("snapshots." + bucketSuffix.value).toHttp))(ivy)
      ),

      /* Publishing by default is public, maven-style and with the same `bucketSuffix` as for resolving */
      isPrivate := false,
      publishMavenStyle := true,
      publishBucketSuffix := bucketSuffix.value,
      publishS3Resolver := {
        val privacy = if (isPrivate.value) "private." else ""
        val prefix = if (isSnapshot.value) "snapshots" else "releases"
        val address = privacy+prefix+"."+publishBucketSuffix.value 
        s3resolver.value(address+" S3 publishing bucket", s3(address)).
          withPatterns(if(publishMavenStyle.value) mvn else ivy)
      },
      publishTo := Some(publishS3Resolver.value)
    )

}
