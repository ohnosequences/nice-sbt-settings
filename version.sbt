lazy val Snap = config("snap")
inConfig(Snap)(
  Classpaths.ivyBaseSettings ++
  Classpaths.ivyPublishSettings ++
  Defaults.configSettings ++
  Seq(
    packageBin <<= packageBin.in(Compile),
    packagedArtifacts <<= Classpaths.packaged(
      makePom +: (Classpaths.defaultPackageKeys).map { _ in Snap }
    )
  )
)
// inConfig(Snap)(Seq
// )

version in Snap := "0.8.0-SNAPSHOT"
publishTo in Snap := Some(publishS3Resolver.value)
// isSnapshot in Snap := true
