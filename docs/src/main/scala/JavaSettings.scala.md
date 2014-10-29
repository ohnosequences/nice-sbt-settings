## Java-related settings

This module defines settings that are specific to Java projects


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

object JavaSettings extends sbt.Plugin {
```

### Settings 

Java version can be `"1.6"` or `"1.7"`


```scala
  lazy val javaVersion = settingKey[String]("Java version")

  lazy val javaSettings: Seq[Setting[_]] = Seq(
    // default is Java 7
    javaVersion := "1.7",

    // to omit _2.10 suffixes:
    crossPaths := false,
    // to omit scala library dependency
    autoScalaLibrary := false,

    javacOptions ++= Seq(
      "-source", javaVersion.value,
      "-target", javaVersion.value,
      "-Xlint:unchecked",
      "-encoding", "UTF-8"
    ),

    // javadoc doesn't know about source/target 1.7
    javacOptions in (Compile, doc) := Seq()
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