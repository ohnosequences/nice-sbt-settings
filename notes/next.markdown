* Major changes:
  - #47: Translated all settings-modules to [auto-plugins](http://www.scala-sbt.org/0.13/docs/Plugins.html) with proper dependencies between them. They will be [enabled](http://www.scala-sbt.org/0.13/docs/Using-Plugins.html#Enabling+and+disabling+auto+plugins) automatically, so you can disable some of them explicitly if needed.  
    The only plugin that doesn't enable itself automatically the one for Java-only projects. To switch it on you have to add `enablePlugin(JavaOnlySettings)` to your `build.sbt`.
  - #45: The new `AssemblySettings` auto-plugin loads fat-jar related settings automatically, but to publish the generated artifact, you need to add `addFatArtifactPublishing(<config>)` setting to your `build.sbt` explicitly (default `<config>` is `Compile`).


* Upgrades (#43, #44, #46, #48):
  - Default Scala version: `2.11.7 -> 2.11.8`
  - [sbt-s3-resolver](https://github.com/ohnosequences/sbt-s3-resolver): `0.13.0 -> 0.14.0`
  - [sbt-release](https://github.com/sbt/sbt-release): `1.0.1 -> 1.0.3`
  - [sbt-assembly](https://github.com/sbt/sbt-assembly): `0.14.0 -> 0.14.3`
  - [sbt-updates](https://github.com/rtimush/sbt-updates): `0.1.9 -> 0.1.10`
  - [sbt-wartremover](https://github.com/puffnfresh/wartremover): `0.14 -> 1.0.1`. Default list of warts now is [`Warts.unsafe`](https://github.com/puffnfresh/wartremover#unsafe)

See the full list of the merged pull-requests for this version: [v0.8.0 milestone](https://github.com/ohnosequences/nice-sbt-settings/issues?q=milestone%3Av0.8.0).
