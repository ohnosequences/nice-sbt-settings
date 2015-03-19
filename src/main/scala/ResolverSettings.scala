/* ## Resolver-related settings

   This module defines resolvers for library dependencies and publishing
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import ohnosequences.sbt.SbtS3Resolver.autoImport._

object ResolverSettings extends sbt.Plugin {

  /* ### Setting keys */

  lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
  lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
  lazy val bucketRegion = settingKey[String]("Amazon S3 bucket region")
  lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
  lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")

  /* ### Settings */

  // Just some aliases for the patterns
  val mvn = Resolver.mavenStylePatterns
  val ivy = Resolver.ivyStylePatterns

  def s3https(region: String, bucket: String): String = s"https://s3-${region}.amazonaws.com/${bucket}"

  lazy val resolverSettings: Seq[Setting[_]] = 
    Seq(
      /* Adding default maven/ivy resolvers with the default `bucketSuffix` */
      bucketSuffix := organization.value + ".com",
      bucketRegion := "eu-west-1",
      resolvers ++= Seq[Resolver]( 
        organization.value + " public maven releases"  at s3https(bucketRegion.value, "releases." + bucketSuffix.value),
        organization.value + " public maven snapshots" at s3https(bucketRegion.value, "snapshots." + bucketSuffix.value),
        Resolver.url(organization.value + " public ivy releases", url(s3https(bucketRegion.value, "releases." + bucketSuffix.value)))(ivy),
        Resolver.url(organization.value + " public ivy snapshots", url(s3https(bucketRegion.value, "snapshots." + bucketSuffix.value)))(ivy)
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
