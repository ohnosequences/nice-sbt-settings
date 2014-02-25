Nice.scalaProject

sbtPlugin := true

name := "nice-sbt-settings"

description := "sbt plugin accumulating some useful and nice sbt settings"

organization := "ohnosequences"

bucketSuffix := "era7.com"

resolvers ++= Seq(
  "sbt-taglist-releases" at "http://johanandren.github.com/releases/",
  "laughedelic maven releases" at "http://dl.bintray.com/laughedelic/maven",
  Resolver.url("laughedelic sbt-plugins", url("http://dl.bintray.com/laughedelic/sbt-plugins"))(Resolver.ivyStylePatterns)
)

dependencyOverrides += "org.apache.ivy" % "ivy" % "2.3.0"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.8.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.10.1")

resolvers += Resolver.url("sbt-plugin-snapshots", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.4-SNAPSHOT")

addSbtPlugin("laughedelic" % "literator-plugin" % "0.5.0")

addSbtPlugin("com.markatta" % "taglist-plugin" % "1.3")

// lazy val p = project in file(".") dependsOn RootProject(uri("git://github.com/rtimush/sbt-updates.git"))
