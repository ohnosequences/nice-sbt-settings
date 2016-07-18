## Java-related settings

This module defines settings that are specific to purely-Java projects


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

case object JavaOnlySettings extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin
  // NOTE: it means that the plugin has to be manually activated: `enablePlugin(JavaOnlySettings)`
  override def trigger = noTrigger

  case object autoImport {

    lazy val javaVersion = settingKey[String]("Java version")
  }
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // default is Java 8
    javaVersion := "1.8",

    // to omit _2.10 suffixes:
    crossPaths := false,
    // to omit scala library dependency
    autoScalaLibrary := false,

    javacOptions ++= Seq(
      "-source", javaVersion.value,
      "-target", javaVersion.value,
      "-Xlint:unchecked",
      "-encoding", "UTF-8"
    ),

    // javadoc doesn't know about source/target 1.7
    javacOptions in (Compile, doc) := Seq()
  )

}

```




[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaOnlySettings.scala]: JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md
[main/scala/WartRemoverSettings.scala]: WartRemoverSettings.scala.md