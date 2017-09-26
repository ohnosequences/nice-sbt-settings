/* ## Linting settings

   This module adds settings to use the [wartremover](https://github.com/typelevel/wartremover)
   linting plugin, which warns you about different "warts" in your code.
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import wartremover._

case object WartRemoverSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = WartRemover


  private val defaultWartWarnings = Seq(
    Wart.Throw,
    Wart.DefaultArguments
  )

  // All unsafe minus those that have too many false-positives
  private val defaultWartErrors = Warts.unsafe diff Seq(
    Wart.Any,
    Wart.NonUnitStatements
  ) diff defaultWartWarnings

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    wartremoverWarnings in (Compile, compile) := defaultWartWarnings,
    wartremoverWarnings in (Test,    compile) := defaultWartWarnings,
    wartremoverErrors   in (Compile, compile) := defaultWartErrors,
    wartremoverErrors   in (Test,    compile) := defaultWartErrors
  )

}
