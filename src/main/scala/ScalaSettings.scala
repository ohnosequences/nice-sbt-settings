/* ## Scala-related settings

   This module defines settings that are specific to Scala
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import DocumentationSettings._

object ScalaSettings extends sbt.Plugin {

  /* ### Settings */

  lazy val scalaSettings: Seq[Setting[_]] = Seq(
    /* This doesn't allow any conflicts in dependencies: */
    conflictManager := ConflictManager.strict,

    scalaVersion := "2.11.5",
    // we don't want these versions to conflict:
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
      "-Xlint"
    ),

    /* full cleaning */
    cleanFiles ++= Seq(
      baseDirectory.value / "project/target",
      // NOTE: we assume here, that you don't have a super-meta-nested-sbt-project
      baseDirectory.value / "project/project",
      (target in (Compile, doc)).value
    )
  )

}
