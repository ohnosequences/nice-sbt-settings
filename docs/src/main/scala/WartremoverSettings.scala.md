## Linting settings

This module adds settings to use the [wartremover](https://github.com/typelevel/wartremover)
linting plugin, which warns you about different "warts" in your code.


```scala
package ohnosequences.sbt.nice

import sbt._, Keys._
import wartremover._

object WartRemoverSettings extends sbt.AutoPlugin {

  override def requires = WartRemover
  override def trigger = allRequirements
```

### Settings

```scala
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= Warts.unsafe
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