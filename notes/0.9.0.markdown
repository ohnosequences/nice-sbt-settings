* #58: Upgraded to sbt to 1.0.2
* #60: Removed enforced scalatest dependency, once you add it to a project explicitly release-only test tag will be generated automatically
* #62: Dropped literator plugin dependency and the source-docs generation from the release process
* #32: Changed default wartremover settings
* Removed hardcoded `scalaVersion`, it should be set by sbt defaults
* Replaced `bucketRegion` setting with `s3region` from sbt-s3-resolver
