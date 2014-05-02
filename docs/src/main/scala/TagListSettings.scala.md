## TagList-related settings

This module adds settings to use the [sbt-taglist plugin](https://github.com/johanandren/sbt-taglist)
which helps to warn that you have tags in your code indicating that something should be fixed.


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._
import TagListPlugin._

object TagListSettings extends sbt.Plugin {
```

### Settings

```scala
  lazy val tagListSettings: Seq[Setting[_]] = {
    TagListPlugin.tagListSettings ++ Seq(
      TagListKeys.tags := Set(
        // Tag("note", TagListPlugin.Info),
        Tag("todo", TagListPlugin.Warn), 
        Tag("fixme", TagListPlugin.Warn)
      )
    )
  }

}

```


------

### Index

+ src
  + main
    + scala
      + [AssemblySettings.scala][main/scala/AssemblySettings.scala]
      + [DocumentationSettings.scala][main/scala/DocumentationSettings.scala]
      + [JavaSettings.scala][main/scala/JavaSettings.scala]
      + [MetadataSettings.scala][main/scala/MetadataSettings.scala]
      + [NiceProjectConfigs.scala][main/scala/NiceProjectConfigs.scala]
      + [ReleaseSettings.scala][main/scala/ReleaseSettings.scala]
      + [ResolverSettings.scala][main/scala/ResolverSettings.scala]
      + [ScalaSettings.scala][main/scala/ScalaSettings.scala]
      + [TagListSettings.scala][main/scala/TagListSettings.scala]

[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaSettings.scala]: JavaSettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/NiceProjectConfigs.scala]: NiceProjectConfigs.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md