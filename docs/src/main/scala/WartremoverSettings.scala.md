## Linting settings

This module adds settings to use the [wartremover](https://github.com/typelevel/wartremover) 
linting plugin, which warns you about different "warts" in your code.


```scala
package ohnosequences.sbt.nice

import sbt._
import Keys._

import com.markatta.sbttaglist._
import wartremover._

object WartremoverSettings extends sbt.Plugin {
```

### Settings

```scala
  private val defaultWarts = Seq(
      Wart.Any2StringAdd,
      Wart.AsInstanceOf,
      Wart.EitherProjectionPartial,
      Wart.IsInstanceOf,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Product,
      Wart.Return,
      Wart.Serializable,
      Wart.Var,
      Wart.ListOps
    )

  lazy val wartremoverSettings: Seq[Setting[_]] = Seq(
    wartremoverErrors in (Compile, compile) ++= defaultWarts
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