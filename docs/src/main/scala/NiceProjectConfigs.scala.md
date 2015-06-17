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
```

You can just say somewhere **in the very beginning** of your `build.sbt`:

```scala
Nice.scalaProject

// and then you can adjust any settings
```


```scala
    lazy val scalaProject: Seq[Setting[_]] =
      MetadataSettings.metadataSettings ++
      ScalaSettings.scalaSettings ++
      ResolverSettings.resolverSettings ++
      DocumentationSettings.documentationSettings ++
      ReleaseSettings.releaseSettings ++
      TagListSettings.tagListSettings ++
      WartremoverSettings.wartremoverSettings
```

Same for `Nice.javaProject` - it includes all `scalaProject` settings,
Note that default java version is 1.7. You can change it after loading these settings:

```scala
Nice.javaProject

javaVersion := "1.8"
```


```scala
    lazy val javaProject: Seq[Setting[_]] =
      scalaProject ++
      JavaSettings.javaSettings

  }

}

```




[main/scala/AssemblySettings.scala]: AssemblySettings.scala.md
[main/scala/DocumentationSettings.scala]: DocumentationSettings.scala.md
[main/scala/JavaSettings.scala]: JavaSettings.scala.md
[main/scala/MetadataSettings.scala]: MetadataSettings.scala.md
[main/scala/NiceProjectConfigs.scala]: NiceProjectConfigs.scala.md
[main/scala/ReleaseSettings.scala]: ReleaseSettings.scala.md
[main/scala/ResolverSettings.scala]: ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ScalaSettings.scala.md
[main/scala/TagListSettings.scala]: TagListSettings.scala.md
[main/scala/WartremoverSettings.scala]: WartremoverSettings.scala.md