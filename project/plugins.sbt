resolvers ++= Seq(
  "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com",
  "laughedelic maven releases" at "http://dl.bintray.com/laughedelic/maven",
  Resolver.url("laughedelic sbt-plugins", url("http://dl.bintray.com/laughedelic/sbt-plugins"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.3.2")
