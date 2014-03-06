/* ## TagList-related settings

   This module adds settings to use the [sbt-taglist plugin](https://github.com/johanandren/sbt-taglist)
   which helps to warn that you have tags in your code indicating that something should be fixed.
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._
import TagListPlugin._

object TagListSettings extends sbt.Plugin {

  /* ### Settings */

  lazy val tagListSettings: Seq[Setting[_]] = {
    TagListPlugin.tagListSettings ++ Seq(
      TagListKeys.tags := Set(
        // Tag("note", TagListPlugin.Info),
        Tag("todo", TagListPlugin.Info), 
        Tag("fixme", TagListPlugin.Warn)
      ),
      compile := {
        val _ = TagListKeys.tagList.value
        (compile in Compile).value
      }
    )
  }

}
