/* ## Statika artifact metadata settings

   This plugin adds a dependency on Statika and generates artifact metadata for bundles.
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import sbtbuildinfo._, BuildInfoKeys._

object StatikaBundleSettings extends sbt.AutoPlugin {

  // NOTE: it means that the plugin has to be manually activated: `enablePlugin(JavaOnlySettings)`
  override def trigger  = noTrigger
  override def requires =
    sbtbuildinfo.BuildInfoPlugin &&
    AssemblySettings

  case object autoImport {

    lazy val statikaVersion = settingKey[String]("Statika library version")
  }
  import autoImport._

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    statikaVersion := "2.0.0-M5",
    libraryDependencies += "ohnosequences" %% "statika" % statikaVersion.value,

    buildInfoPackage := "generated.metadata",
    buildInfoObject  := normalizedName.value,
    buildInfoOptions := Seq(BuildInfoOption.Traits("ohnosequences.statika.AnyArtifactMetadata")),
    buildInfoKeys    := Seq[BuildInfoKey](
      organization,
      version,
      "artifact" -> normalizedName.value,
      "artifactUrl" -> AssemblySettings.autoImport.fatArtifactUrl.value
    )
  )

}
