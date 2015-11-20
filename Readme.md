## Nice sbt settings plugin  

[![License](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/github/release/ohnosequences/nice-sbt-settings.svg)](https://github.com/ohnosequences/nice-sbt-settings/releases/latest)

This SBT plugin aims to standardize and simplify the configuration of all Scala and Java era7/ohnosequences/bio4j sbt-based projects.

### Usage

To start using this plugin add the following to your `project/plugins.sbt` file:

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "<version>")
```

> **Note**: you should use sbt `v0.13.5+`.


### Settings and configuration

This plugin includes

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

## Contact / help

This project is maintained by [@laughedelic](https://github.com/laughedelic). Join the chat-room if you want to ask or discuss something
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/ohnosequences/nice-sbt-settings?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


[JavaSettings]: docs/src/main/scala/JavaSettings.scala.md
[ScalaSettings]: docs/src/main/scala/ScalaSettings.scala.md
[ResolverSettings]: docs/src/main/scala/ResolverSettings.scala.md
[DocumentationSettings]: docs/src/main/scala/DocumentationSettings.scala.md
[ReleaseSettings]: docs/src/main/scala/ReleaseSettings.scala.md
[MetadataSettings]: docs/src/main/scala/MetadataSettings.scala.md
[AssemblySettings]: docs/src/main/scala/AssemblySettings.scala.md
[TagListSettings]: docs/src/main/scala/TagListSettings.scala.md
[NiceProjectConfigs]: docs/src/main/scala/NiceProjectConfigs.scala.md
