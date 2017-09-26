sbtPlugin := true

name := "nice-sbt-settings"
organization := "ohnosequences"
description := "sbt plugin accumulating some useful and nice sbt settings"

scalaVersion := "2.12.3"
sbtVersion in Global := "1.0.2"

bucketSuffix := "era7.com"

resolvers += Resolver.jcenterRepo
resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"

addSbtPlugin("ohnosequences"     % "sbt-s3-resolver"    % "0.18.0")  // https://github.com/ohnosequences/sbt-s3-resolver
addSbtPlugin("ohnosequences"     % "sbt-github-release" % "0.5.0")   // https://github.com/ohnosequences/sbt-github-release
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"       % "0.14.5")  // https://github.com/sbt/sbt-assembly
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"        % "0.3.1")   // https://github.com/rtimush/sbt-updates
addSbtPlugin("com.markatta"      % "sbt-taglist"        % "1.4.0")   // https://github.com/johanandren/sbt-taglist
addSbtPlugin("org.wartremover"   % "sbt-wartremover"    % "2.2.1")   // https://github.com/puffnfresh/wartremover

// libraryDependencies ++= Seq(
//   "com.amazonaws" % "aws-java-sdk-s3" % "1.11.27"
// )
dependencyOverrides ++= Set(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
)

wartremoverErrors in (Compile, compile) := Seq()
// wartremoverWarnings ++= Warts.allBut(Wart.NoNeedForMonad)

dependencyOverrides ++= Set(
  "commons-codec"              % "commons-codec"    % "1.10",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.4",
  "org.apache.httpcomponents"  % "httpclient"       % "4.3.6",
  "com.jcraft"                 % "jsch"             % "0.1.50",
  "joda-time"                  % "joda-time"        % "2.8"
)
