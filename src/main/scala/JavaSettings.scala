/* ## Java-related settings

   This module defines settings that are specific to Java projects
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

object JavaSettings extends sbt.Plugin {


  /* ### Settings 

     Java version can be `"1.6"` or `"1.7"`
  */

  lazy val javaVersion = settingKey[String]("Java version")

  lazy val javaSettings: Seq[Setting[_]] = Seq(
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
