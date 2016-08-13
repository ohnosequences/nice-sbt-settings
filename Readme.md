## Nice sbt settings plugin  

[![](http://github-release-version.herokuapp.com/github/ohnosequences/nice-sbt-settings/release.svg)](https://github.com/ohnosequences/nice-sbt-settings/releases/latest)
[![](https://img.shields.io/github/release/ohnosequences/nice-sbt-settings.svg)](https://github.com/ohnosequences/nice-sbt-settings/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/nice-sbt-settings)

This SBT plugin aims to standardize and simplify configuration of all Scala and Java [era7](https://github.com/era7bio)/[ohnosequences](https://github.com/ohnosequences)/[bio4j](https://github.com/bio4j) sbt-based projects.

### Features

- Git-based version management
- Automated release process with
  + various checks:
    * TODO/FIXME notes
    * dependencies updates
    * release notes
  + custom artifacts publishing (to Amazon S3)
  + release-only tests
  + Github release publishing
  + source/API documentation publishing

This plugin is based on the following ones:

- [sbt-s3-resolver](https://github.com/ohnosequences/sbt-s3-resolver)
- [sbt-github-release](https://github.com/ohnosequences/sbt-github-release)
- [sbt-assembly](https://github.com/sbt/sbt-assembly)
- [sbt-updates](https://github.com/rtimush/sbt-updates)
- [literator](https://github.com/laughedelic/literator)
- [sbt-taglist](https://github.com/johanandren/sbt-taglist)
- [wartremover](https://github.com/puffnfresh/wartremover)
- plus add a couple of new ones: for git-versioning and releases


### Usage

In `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "<version>")
```

> **Note**: you should use sbt `v0.13.5+`.
