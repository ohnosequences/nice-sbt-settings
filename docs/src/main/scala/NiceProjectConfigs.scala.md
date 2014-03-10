## Project configurations

This is the module defining project configurations, which are 
just combinations of the setting sets defined in other modules.


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._
import sbt.Extracted

object NiceProjectConfigs extends sbt.Plugin {
  
  object Nice {

    import ScalaSettings._
    import ResolverSettings._
    import DocumentationSettings._
    import TagListSettings._
    import ReleaseSettings._
```

You can just say somewhere **in the very beginning** of your `build.sbt`:

```scala
Nice.scalaProject

// and then you can adjust any settings
```


```scala
    lazy val scalaProject: Seq[Setting[_]] =
      metainfoSettings ++
      scalaSettings ++
      resolverSettings ++
      documentationSettings ++
      releaseSettings ++
      tagListSettings


    import JavaSettings._
```

Same for `Nice.javaProject` — it includes all `scalaProject` settings

```scala
    lazy val javaProject: Seq[Setting[_]] =
      scalaProject ++
      javaSettings

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
      + [NiceProjectConfigs.scala][main/scala/NiceProjectConfigs.scala]
      + [ReleaseSettings.scala][main/scala/ReleaseSettings.scala]
      + [ResolverSettings.scala][main/scala/ResolverSettings.scala]
      + [ScalaSettings.scala][main/scala/ScalaSettings.scala]
      + [TagListSettings.scala][main/scala/TagListSettings.scala]

[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaSettings.scala]: JavaSettings.scala.md
[main/scala/NiceProjectConfigs.scala]: NiceProjectConfigs.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md