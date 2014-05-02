## Nice sbt settings plugin

This is an SBT plugin, aimed to standardize and simplify configuration of all era7/ohnosequences sbt-based projects.


### Usage

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com",

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.4.0")
```

> **Note**: you should use sbt `v0.13`.


### Setting groups

This plugin icludes

* [Java related settings][JavaSettings]
* [Scala related settings][ScalaSettings]
* [Resolver settings][ResolverSettings]
* [Settings for generating documentation][DocumentationSettings]
* [Release process settings][ReleaseSettings]
* [Project metadata settings][MetadataSettings]
* [Sbt-assembly plugin related settings][AssemblySettings]
* [TagList plugin settings][TagListSettings]

And defines two sets of these settings: [for java and for scala projects][NiceProjectConfigs].

Just follow the links, as all docs are there (generated from the comments in the code).



[JavaSettings]: docs/src/main/scala/JavaSettings.scala.md
[ScalaSettings]: docs/src/main/scala/ScalaSettings.scala.md
[ResolverSettings]: docs/src/main/scala/ResolverSettings.scala.md
[DocumentationSettings]: docs/src/main/scala/DocumentationSettings.scala.md
[ReleaseSettings]: docs/src/main/scala/ReleaseSettings.scala.md
[MetadataSettings]: docs/src/main/scala/MetadataSettings.scala.md
[AssemblySettings]: docs/src/main/scala/AssemblySettings.scala.md
[TagListSettings]: docs/src/main/scala/TagListSettings.scala.md
[NiceProjectConfigs]: docs/src/main/scala/NiceProjectConfigs.scala.md
