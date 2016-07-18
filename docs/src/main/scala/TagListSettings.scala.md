## TagList-related settings

This module adds settings to use the [sbt-taglist plugin](https://github.com/johanandren/sbt-taglist)
which helps to warn that you have tags in your code indicating that something should be fixed.


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._, TagListPlugin._

object TagListSettings extends sbt.AutoPlugin {

  override def requires = empty // should be TagListPlugin, but it's not an autoplugin
  override def trigger = allRequirements
```

### Settings

```scala
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

```




[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaOnlySettings.scala]: JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md
[main/scala/WartRemoverSettings.scala]: WartRemoverSettings.scala.md