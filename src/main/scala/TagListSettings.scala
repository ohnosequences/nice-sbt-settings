/* ## TagList-related settings

   This module adds settings to use the [sbt-taglist plugin](https://github.com/johanandren/sbt-taglist)
   which helps to warn that you have tags in your code indicating that something should be fixed.
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._, TagListPlugin._

object TagListSettings extends sbt.AutoPlugin {

  override def requires = empty // should be TagListPlugin, but it's not an autoplugin
  override def trigger = allRequirements

  /* ### Settings */
  override lazy val projectSettings: Seq[Setting[_]] =
    TagListPlugin.tagListSettings ++
    Seq(
      TagListKeys.tags := Set(
        Tag("note", TagListPlugin.Info),
        Tag("todo", TagListPlugin.Warn),
        Tag("fixme", TagListPlugin.Warn)
      )
    )

}
