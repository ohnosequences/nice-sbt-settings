## Era7 sbt release plugin

This is an SBT plugin, aimed to standardaze and simplify the release process for the most of the Era7/ohnosequences projects.

### Usage

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "era7-sbt-settings" % "0.2.0")
```

This plugin includes [sbt-s3-resolver](https://github.com/ohnosequences/sbt-s3-resolver) and [sbt-release](https://github.com/sbt/sbt-release) plugins and adds the following sbt settings:


| Key                   |     Type      | Description                                                       |
|----------------------:|:--------------|:------------------------------------------------------------------|
|     `isPrivate`       |    Boolean    |    If true, publish to private S3 bucket, else to public          |
|     `bucketSuffix`    |  String       |       Amazon S3 bucket suffix for resolvers                       |
| `publishBucketSuffix` | String        |    Amazon S3 bucket suffix for the `publishTo` default resolver   | 
| `publishS3Resolver`   |  S3Resolver   |       S3Resolver which will be used in `publishTo`                |

Also, the plugin provides several sets of predefined settings:

* `Era7.resolversSettings`
  + `bucketSuffix := {organization.value + ".com"}`
  + adds resolvers for maven and ivy snapshots/releases buckets with this suffix
* `Era7.publishingSettings`
  + `isPrivate := false`
  + `publishMavenStyle := true`
  + `publishBucketSuffix := bucketSuffix.value`
  + sets `publishS3Resolver` to something like `<privacy prefix><releases/snapshots prefix>.publishBucketSuffix`
  + sets `publishTo` to this `S3Resolver`, if there are credentials
* `Era7.releaseSettings`
  + sets version bumping strategy is to increase the major version number
* `Era7.allSettings` is a combination of these three sets

So normally, you should use this plugin by putting the following lines _in the beginning_ of the `build.sbt` file:

```scala
import ohnosequences.sbt._

Era7.allSettings

// here your custom settings
```

after that you can customize any settings for your needs (usually it will be the buckets suffixes). The reason, why the order is important, is that if you first set `bucketSuffix` and then add `Era7.resolversSettings`, then it will be overridden.
