## Scala-related settings

This module defines settings that are specific to Scala


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import DocumentationSettings._

object ScalaSettings extends sbt.Plugin {
```

### Settings

```scala
  lazy val scalaSettings: Seq[Setting[_]] = Seq(
```

This doesn't allow any conflicts in dependencies:

```scala
    conflictManager := ConflictManager.strict,

    scalaVersion := "2.11.5",
    // we don't want these versions to conflict:
    dependencyOverrides += "org.scala-lang" % "scala-library" % scalaVersion.value,
    dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    dependencyOverrides += "org.scala-lang" % "scala-reflect" % scalaVersion.value,

    scalacOptions ++= Seq(
      "-feature",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-deprecation",
      "-unchecked",
      "-Xlint"
    ),
```

full cleaning

```scala
    cleanFiles ++= Seq(
      baseDirectory.value / "project/target",
      // NOTE: we assume here, that you don't have a super-meta-nested-sbt-project
      baseDirectory.value / "project/project",
      (target in (Compile, doc)).value
    )
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
      + [MetadataSettings.scala][main/scala/MetadataSettings.scala]
      + [NiceProjectConfigs.scala][main/scala/NiceProjectConfigs.scala]
      + [ReleaseSettings.scala][main/scala/ReleaseSettings.scala]
      + [ResolverSettings.scala][main/scala/ResolverSettings.scala]
      + [ScalaSettings.scala][main/scala/ScalaSettings.scala]
      + [TagListSettings.scala][main/scala/TagListSettings.scala]
      + [WartremoverSettings.scala][main/scala/WartremoverSettings.scala]

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