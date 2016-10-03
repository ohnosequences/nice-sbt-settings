This is the third release candidate for the version 0.8.0. Changes since RC2:

- if you run `publishApiDocs` twice on the same revision it will fail second time because there are no changes (improved the error message)
- statika generated metadata object full name now is

  ```
  <organization>.generated.metadata.<normalizedName>
  ```

  where normalized name is the same as `name` with underscores replaced by dots (should use `name` directly)
- warns on load if `version.sbt` file is present and tell to remove it
- moved `assembly` before artifacts publishing so that if it fails, no artifacts are published at all
- rearranged release-tests and publishing: first upload fat-jar (if needed), then run release tests, then publish artifacts (it is possible now, because `fatJarUpload` is not a part of `publish`)
- picked up improvements in sbt-github-release [v0.4.0](https://github.com/ohnosequences/sbt-github-release/releases/tag/v0.4.0)
- updated various plugin dependencies
