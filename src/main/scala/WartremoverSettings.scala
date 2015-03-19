/* ## Linting settings

   This module adds settings to use the [wartremover](https://github.com/typelevel/wartremover) 
   linting plugin, which warns you about different "warts" in your code.
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._
import wartremover._

object WartremoverSettings extends sbt.Plugin {

  /* ### Settings */

  private val defaultWarts = Seq(
      Wart.Any2StringAdd,
      Wart.AsInstanceOf,
      Wart.EitherProjectionPartial,
      Wart.IsInstanceOf,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Product,
      Wart.Return,
      Wart.Serializable,
      Wart.Var,
      Wart.ListOps
    )

  lazy val wartremoverSettings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= defaultWarts
  )

}
