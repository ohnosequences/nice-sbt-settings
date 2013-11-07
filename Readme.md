## Nice sbt settings plugin

This is an SBT plugin, aimed to standardize and simplify configuration of all era7/ohnosequences sbt-based projects.


### Usage

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.2.0")
```

> **Note**: you should use sbt `v0.13`.


### Setting keys

This plugin includes [sbt-s3-resolver](https://github.com/ohnosequences/sbt-s3-resolver) and [sbt-release](https://github.com/sbt/sbt-release) plugins and adds the following sbt settings:

 Key                   |     Type      | Description
----------------------:|:--------------|:-------------------------------------------------------------
`isPrivate`            | Boolean       | If true, publish to private S3 bucket, else to public
`bucketSuffix`         | String        | Amazon S3 bucket suffix for resolvers
`publishBucketSuffix`  | String        | Amazon S3 bucket suffix for the `publishTo` default resolver
`publishS3Resolver`    | S3Resolver    | S3Resolver which will be used in `publishTo`
`fatArtifactClassifier`| String        | Classifier (suffix) of the fat jar artifact


### Predefined configurations

Also, the plugin provides a sets of predefined settings, combined as follows:

1. `Nice.scalaProject`:
   * `Nice.metainfoSettings`
     + default homepage and organization homepage
     + AGPL-v3 license 
   * `Nice.scalaSettings`
     + strict conflict manager
     + latest stable scala version
     + standard set of scalac options
   * `Nice.resolversSettings`
     + default bucketSuffix
     + resolvers for maven and ivy snapshots/releases buckets with this suffix
   * `Nice.publishingSettings`
     + default `isPrivate` is `false`
     + default `publishBucketSuffix` (same as `bucketSuffix`)
     + publish maven style
     + sets `publishS3Resolver` to something like `<privacy prefix><releases/snapshots prefix>.publishBucketSuffix`
     + sets `publishTo` to this `S3Resolver`, if there are credentials
   * `Nice.releaseSettings`
     + sets version bumping strategy is to increase the major version number
2. `Nice.javaProject`:
   * `Nice.scalaProject`
   * `Nice.javaSettings`
     + excludes Scala library dependency
     + omits `_2.10` artifact suffix

See sources for the real definitions.

So normally, you just put one line _in the beginning_ of the `build.sbt` file:

```scala
Nice.scalaProject

// then your custom settings
```

See [era7bio/scala-2.10.g8](https://github.com/era7bio/scala-2.10.g8) template for example.


#### Optional fat jar artifact

If you want to use [sbt-assembly](https://github.com/sbt/sbt-assembly) for generating and publishing a fat jar artifact together with the normal one, you can _add_ to your configuration:

```
Nice.fatArtifactSettings

fatArtifactClassifier := "chubby"   // optional
```

by default `fatArtifactClassifier` is just `"fat"`.
