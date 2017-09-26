/* ## Resolver-related settings

   This module defines resolvers for library dependencies and publishing
*/
package ohnosequences.sbt.nice

import sbt._, Keys._

import ohnosequences.sbt.SbtS3Resolver.autoImport._

case object ResolverSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires =
    plugins.JvmPlugin &&
    ohnosequences.sbt.SbtS3Resolver

  case object autoImport {

    lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
    lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
    lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
    lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")
  }
  import autoImport._

  /* ### Settings */

  // Just some aliases for the patterns
  private val mvn = Resolver.mavenStylePatterns
  private val ivy = Resolver.ivyStylePatterns

  override def projectSettings: Seq[Setting[_]] = Seq(
    /* Adding default maven/ivy resolvers with the default `bucketSuffix` */
    bucketSuffix := organization.value + ".com",
    s3region := com.amazonaws.regions.Regions.EU_WEST_1,

    resolvers := Seq[Resolver](
      organization.value + " public maven releases"  at s3("releases."  + bucketSuffix.value).toHttps(s3region.value),
      organization.value + " public maven snapshots" at s3("snapshots." + bucketSuffix.value).toHttps(s3region.value),
      Resolver.url(organization.value + " public ivy releases",  url(s3("releases."  + bucketSuffix.value).toHttps(s3region.value)))(ivy),
      Resolver.url(organization.value + " public ivy snapshots", url(s3("snapshots." + bucketSuffix.value).toHttps(s3region.value)))(ivy)
    ) ++ resolvers.value,

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
