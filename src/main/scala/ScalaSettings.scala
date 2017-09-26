/* ## Scala-related settings

   This module defines the most basic settings that are specific to Scala
*/
package ohnosequences.sbt.nice

import sbt._, Keys._

case object ScalaSettings extends sbt.AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    /* This doesn't allow any conflicts in dependencies: */
    conflictManager := ConflictManager.strict,

    /* Circular dependencies are prohibited */
    updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error),

    scalacOptions ++= Seq(
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation",
      "-unchecked",
      "-Xlint"
    ),

    /* Full cleaning */
    cleanFiles ++= Seq(
      baseDirectory.value / "project/target",
      // NOTE: we assume here, that you don't have a super-meta-nested-sbt-project
      baseDirectory.value / "project/project",
      (target in (Compile, doc)).value
    )
  )

}
