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
  lazy val metainfoSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("https://github.com/"+organization.value+"/"+name.value)),
    organizationHomepage := Some(url("http://"+organization.value+".com")),
    licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
  )

  lazy val scalaSettings: Seq[Setting[_]] = Seq(
    // this doesn't allow any conflicts in dependencies:
    conflictManager := ConflictManager.strict,

    scalaVersion := "2.10.3",
    // 2.10.x are compatible and we want to use the latest _for everything_:
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
      "-Xlint",
      "-target:jvm-1.7"
    ),

    // full cleaning
    cleanFiles ++= Seq(
      baseDirectory.value / "project/target",
      // NOTE: we assume here, that you don't have a super-meta-sbt-project
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