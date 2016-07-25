/* ## Resolver-related settings

   This module defines resolvers for library dependencies and publishing
*/
package ohnosequences.sbt.nice

import sbt._, Keys._

import ohnosequences.sbt.SbtS3Resolver.autoImport._

object ResolverSettings extends sbt.AutoPlugin {

  override def requires = ohnosequences.sbt.SbtS3Resolver
  override def trigger = allRequirements

  case object autoImport {

    lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
    lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
    lazy val bucketRegion = settingKey[String]("Amazon S3 bucket region")
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
    bucketRegion := "eu-west-1",

    resolvers := Seq[Resolver](
      organization.value + " public maven releases"  at s3("releases."  + bucketSuffix.value).toHttps(bucketRegion.value),
      organization.value + " public maven snapshots" at s3("snapshots." + bucketSuffix.value).toHttps(bucketRegion.value),
      Resolver.url(organization.value + " public ivy releases",  url(s3("releases."  + bucketSuffix.value).toHttps(bucketRegion.value)))(ivy),
      Resolver.url(organization.value + " public ivy snapshots", url(s3("snapshots." + bucketSuffix.value).toHttps(bucketRegion.value)))(ivy)
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
    publishTo := {
      /* This prevents from publishing snapshots: */
      if (isSnapshot.value) None else Some(publishS3Resolver.value)
    },

    // Just a command for the publish task with a custom error message in case of a snapshot version
    commands += Command.command("publishReloaded") { state =>
      state.log.info(s"Current version: ${Project.extract(state).get(Keys.version)}")

      if ( Project.extract(state).get(Keys.isSnapshot) ) {

        state.log.error("You shouldn't publish snapshots. Commit the changes and try again.")
        state.fail
      } else {

        Project.runTask(publish, state) match {
          case None => state.log.warn("Key wasn't defined"); state.fail
          case Some((newState, Inc(_))) => newState // incomplete
          case Some((newState, Value(_))) => newState // success
        }
      }
    },

    // Shadowing publish task with this command to do reload before actually publishing
    commands += Command.command("publish") { state =>
      "reload" :: "publishReloaded" :: state
    }
  )

}
