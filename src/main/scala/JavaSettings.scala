/* ## Java-related settings

   This module defines settings that are specific to Java
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

object JavaSettings extends sbt.Plugin {


  /* ### Settings */

  lazy val javaSettings: Seq[Setting[_]] = Seq(
    // to omit _2.10 suffixes:
    crossPaths := false,
    // to omit scala library dependency
    autoScalaLibrary := false,

    javacOptions ++= Seq(
      "-source", "1.7",
      "-target", "1.7",
      "-Xlint:unchecked",
      "-encoding", "UTF-8"
    ),

    // javadoc doesn't know about source/target 1.7
    javacOptions in (Compile, doc) := Seq()
    )

}
