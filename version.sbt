lazy val Snap = config("snap") extend(Compile)
inConfig(Snap)(
  Classpaths.ivyBaseSettings ++
  Classpaths.ivyPublishSettings ++
  Defaults.configSettings ++
  Seq(
    packageBin <<= packageBin.in(Compile),
    version in packageBin := version.in(Snap).value,
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
