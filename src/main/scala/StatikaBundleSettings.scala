/* ## Statika artifact metadata settings

   This plugin adds a dependency on Statika and generates artifact metadata for bundles.
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import AssemblySettings.autoImport._

case object StatikaBundleSettings extends sbt.AutoPlugin {

  // NOTE: it means that the plugin has to be manually activated: `enablePlugin(JavaOnlySettings)`
  override def trigger  = noTrigger
  override def requires = AssemblySettings

  case object autoImport {

    def generateStatikaMetadataIn(conf: Configuration): Seq[Setting[_]] = Seq(
      sourceGenerators in conf += generateStatikaMetadataTask(conf).taskValue
    )
  }

  def generateStatikaMetadataTask(conf: Configuration): DefTask[Seq[File]] = Def.task {
    val file = sourceManaged.in(conf).value / "statikaMetadata.scala"

    lazy val parts = organization.value.split('.') ++ name.value.split('.')
    lazy val pkg = (parts.init ++ Seq("generated", "metadata")).mkString(".")
    lazy val obj = parts.last.replaceAll("-", "_")

    IO.write(file, s"""
      |package ${pkg}
      |
      |case object ${obj} extends ohnosequences.statika.AnyArtifactMetadata {
      |  val organization: String = "${organization.value}"
      |  val artifact: String     = "${moduleName.value}"
      |  val version: String      = "${version.value}"
      |  val artifactUrl: String  = "${fatArtifactUrl.value}"
      |}
      |""".stripMargin
    )

    Seq(file)
  }
}
