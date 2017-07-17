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

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) := Warts.unsafe,
    wartremoverErrors in (Test,    compile) := Warts.unsafe
  )

}
