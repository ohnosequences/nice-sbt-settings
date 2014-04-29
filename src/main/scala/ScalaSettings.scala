/* ## Scala-related settings

   This module defines settings that are specific to Scala
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import DocumentationSettings._

object ScalaSettings extends sbt.Plugin {

  /* ### Settings */

  lazy val metainfoSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("https://github.com/"+organization.value+"/"+name.value)),
    organizationHomepage := Some(url("http://"+organization.value+".com")),
    licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
  )

  lazy val scalaSettings: Seq[Setting[_]] = Seq(
    // this doesn't allow any conflicts in dependencies:
    conflictManager := ConflictManager.strict,

    scalaVersion := "2.10.4",
    // we want to use the latest _for everything_:
    dependencyOverrides += "org.scala-lang" % "scala-library" % scalaVersion.value,
    dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    dependencyOverrides += "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    scalacOptions ++= Seq(
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-target:jvm-1.7"
    ),

    // full cleaning
    cleanFiles ++= Seq(
      baseDirectory.value / "project/target",
      // NOTE: we assume here, that you don't have a super-meta-sbt-project
      baseDirectory.value / "project/project",
      (target in (Compile, doc)).value
    )
  )

}
