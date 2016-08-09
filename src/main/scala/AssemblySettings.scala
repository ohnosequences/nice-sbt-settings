/* ## Sbt-assembly-related settings

   This module defines settings to generate fat jars using [sbt-assembly plugin](https://github.com/softprops/assembly-sbt)
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtassembly._
import AssemblyKeys._

case object AssemblySettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires =
    sbtassembly.AssemblyPlugin &&
    ResolverSettings

  case object autoImport {

    /* Classifier is the suffix appended to the artifact name */
    lazy val fatArtifactClassifier = settingKey[String]("Classifier of the fat jar artifact")
    /* This setting holds the url of the published fat artifact */
    lazy val fatArtifactUrl = settingKey[String]("URL of the published fat artifact")

    /* Note, that these settings are not included by default. To turn them on them, add to your
       `build.sbt` `addFatArtifactPublishing()` line (without any prefix) */
    def addFatArtifactPublishingIn(conf: Configuration): Seq[Setting[_]] = Seq(
      artifact in (conf, assembly) := {
        val art = (artifact in (conf, assembly)).value
        art.copy( classifier = Some(fatArtifactClassifier.value) )
      }
    ) ++
    inConfig(conf)(
      addArtifact(artifact in (conf, assembly), assembly)
    )
  }
  import autoImport._

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // suffix for the fat artifact:
    fatArtifactClassifier := "fat",
    // turning off tests in assembly:
    test in assembly := {},

    // mvn: "[organisation]/[module]_[scalaVersion]/[revision]/[artifact]-[revision]-[classifier].[ext]"
    // ivy: "[organisation]/[module]_[scalaVersion]/[revision]/[type]s/[artifact]-[classifier].[ext]"
    fatArtifactUrl := {
      val isMvn = publishMavenStyle.value
      val scalaV = "_"+scalaBinaryVersion.value
      val module = moduleName.value + scalaV
      val artifact = Seq(
        if (isMvn) "" else "jars/",
        module,
        if (isMvn) s"-${version.value}" else "",
        s"-${fatArtifactClassifier.value}",
        ".jar"
      ).mkString

      Seq(
        ResolverSettings.autoImport.publishS3Resolver.value.url,
        organization.value,
        module,
        version.value,
        artifact
      ).mkString("/")
    }
  )

}
