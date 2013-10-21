## Era7 sbt release plugin

This is an SBT plugin, aimed to standardaze and simplify the release process for the most of the Era7/ohnosequences projects.

### Usage

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases"  at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "era7-sbt-release" % "0.1.0")
```

This plugin includes [sbt-s3-resolver](https://github.com/ohnosequences/sbt-s3-resolver) and [sbt-release](https://github.com/sbt/sbt-release) plugins and adds the following sbt settings:

Key                  Type        Description
-------------------  ----------  -------------------------------
isPrivate            Boolean     If true, publish to private S3 bucket, else to public
bucketSuffix         String      Amazon S3 bucket suffix for resolvers
publishBucketSuffix  String      Amazon S3 bucket suffix for the `publishTo` default resolver
publishS3Resolver    S3Resolver  S3Resolver which will be used in `publishTo`


