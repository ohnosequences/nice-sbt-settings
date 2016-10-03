## Statika artifact metadata settings

This plugin adds a dependency on Statika and generates artifact metadata for bundles.


```scala
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

```




[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/Git.scala]: Git.scala.md
[main/scala/JavaOnlySettings.scala]: JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/package.scala]: package.scala.md
[main/scala/release/commands.scala]: release/commands.scala.md
[main/scala/release/keys.scala]: release/keys.scala.md
[main/scala/release/parsers.scala]: release/parsers.scala.md
[main/scala/release/tasks.scala]: release/tasks.scala.md
[main/scala/ReleasePlugin.scala]: ReleasePlugin.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/StatikaBundleSettings.scala]: StatikaBundleSettings.scala.md
[main/scala/Version.scala]: Version.scala.md
[main/scala/VersionSettings.scala]: VersionSettings.scala.md
[main/scala/WartRemoverSettings.scala]: WartRemoverSettings.scala.md