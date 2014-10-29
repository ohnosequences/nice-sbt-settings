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

  lazy val wartremoverSettings: Seq[Setting[_]] = {
    /* We add the same list of warts as `Wart.unsafe` except of `Any` and `NonUnitStatements`.
       See [warts documentation](https://github.com/typelevel/wartremover#warts).
    */
    Seq(wartremoverWarnings in (Compile, compile) ++= Seq(
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
    ))
  }

}
