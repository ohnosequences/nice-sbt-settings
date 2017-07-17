## Java-related settings

This module defines settings that are specific to purely-Java projects


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

case object JavaOnlySettings extends sbt.AutoPlugin {

  // NOTE: it means that the plugin has to be manually activated: `enablePlugin(JavaOnlySettings)`
  override def trigger  = noTrigger
  override def requires = plugins.JvmPlugin

  case object autoImport {

    lazy val javaVersion = settingKey[String]("Java version")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
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