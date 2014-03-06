/* ## Sbt-assembly-related settings

   This module defines settings to generate fat jars using [sbt-assembly plugin](https://github.com/softprops/assembly-sbt)
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtassembly._
import sbtassembly.Plugin._
import AssemblyKeys._

object AssemblySettings extends sbt.Plugin {

  /* ### Setting keys */

  lazy val fatArtifactClassifier = settingKey[String]("Classifier of the fat jar artifact")

  /* ### Settings 

  Note, that these settings are not included by default. To add them, add to your `build.sbt`
  `Nice.fatArtifactSettings` line
  */

  // TODO: check, that this doesn't cause any clashes with another Nice object
  object Nice {
    lazy val fatArtifactSettings: Seq[Setting[_]] =
      (assemblySettings: Seq[Setting[_]]) ++ 
      addArtifact(artifact in (Compile, assembly), assembly) ++ 
      Seq(
        // publishing also a fat artifact:
        fatArtifactClassifier := "fat",
        artifact in (Compile, assembly) :=
          (artifact in (Compile, assembly)).value.copy(
             `classifier` = Some(fatArtifactClassifier.value)
          ),
        test in assembly := {}
      )
  }

}
