## Sbt-assembly-related settings

This module defines settings to generate fat jars using [sbt-assembly plugin](https://github.com/softprops/assembly-sbt)


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import sbtassembly._
import AssemblyKeys._

object AssemblySettings extends sbt.Plugin {
```

### Setting keys 

Classifier is the suffix appended to the artifact name


```scala
  lazy val fatArtifactClassifier = settingKey[String]("Classifier of the fat jar artifact")
```

### Settings 

Note, that these settings are not included by default. To turn them on them, add to your 
`build.sbt` `fatArtifactSettings` line (without any prefix)


```scala
  lazy val fatArtifactSettings: Seq[Setting[_]] =
    addArtifact(artifact in (Compile, assembly), assembly) ++ 
    Seq(
      // publishing fat artifact:
      fatArtifactClassifier := "fat",
      artifact in (Compile, assembly) := (artifact in (Compile, assembly)).value.copy(
        classifier = Some(fatArtifactClassifier.value)
      ),
      // turning off tests in assembly:
      test in assembly := {}
    )

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