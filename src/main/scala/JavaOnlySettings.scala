/* ## Java-related settings

   This module defines settings that are specific to purely-Java projects
*/
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
