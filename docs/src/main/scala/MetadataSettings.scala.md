## Project metadata settings

This module defines some [sbt project metadata](http://www.scala-sbt.org/release/docs/Howto/metadata.html):
default homepages and the license.


```scala
package ohnosequences.sbt.nice

import sbt._, Keys._

object MetadataSettings extends sbt.AutoPlugin {

  override def requires = empty
  override def trigger = allRequirements
```

### Settings

```scala
  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("https://github.com/"+organization.value+"/"+name.value)),
    organizationHomepage := Some(url("http://"+organization.value+".com")),
    licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
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