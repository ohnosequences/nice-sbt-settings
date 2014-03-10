## Sbt-assembly-related settings

This module defines settings to generate fat jars using [sbt-assembly plugin](https://github.com/softprops/assembly-sbt)


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtassembly._
import sbtassembly.Plugin._
import AssemblyKeys._

object AssemblySettings extends sbt.Plugin {
```

### Setting keys

```scala
  lazy val fatArtifactClassifier = settingKey[String]("Classifier of the fat jar artifact")
```

### Settings 

  Note, that these settings are not included by default. To add them, add to your `build.sbt`
  `Nice.fatArtifactSettings` line


```scala
  lazy val fatArtifactSettings: Seq[Setting[_]] =
    (assemblySettings: Seq[Setting[_]]) ++ 
    addArtifact(artifact in (Compile, assembly), assembly) ++ 
    Seq(
      // publishing also a fat artifact:
      fatArtifactClassifier := "fat",
      artifact in (Compile, assembly) :=
        (artifact in (Compile, assembly)).value.copy(
           `classifier` = Some(fatArtifactClassifier.value)
        ),
      test in assembly := {}
    )

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