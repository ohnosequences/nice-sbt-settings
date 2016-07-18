/* ## Linting settings

   This module adds settings to use the [wartremover](https://github.com/typelevel/wartremover)
   linting plugin, which warns you about different "warts" in your code.
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import wartremover._

object WartRemoverSettings extends sbt.AutoPlugin {

  override def requires = WartRemover
  override def trigger = allRequirements

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= Warts.unsafe
  )

}
