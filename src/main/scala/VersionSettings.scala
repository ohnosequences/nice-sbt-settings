/* ## Project version settings

   This plugin uses sbt-git to set version to <last_release_tag>-<git_describe_suffix>[-SNAPSHOT (if there are uncommited changes)].
*/
package ohnosequences.sbt.nice

import sbt._, Keys._
import com.typesafe.sbt._, SbtGit.GitKeys._

case object VersionSettings extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires =
    com.typesafe.sbt.GitPlugin

  val VersionRegex = """v([0-9]+.[0-9]+.[0-9]+)-?(.*)?""".r

  /* ### Settings */
  // override def projectSettings: Seq[Setting[_]] =
  override def buildSettings: Seq[Setting[_]] =
    GitPlugin.autoImport.versionWithGit ++ Seq(
      useGitDescribe := true,
      gitTagToVersionNumber := {
        case ver@VersionRegex(_, _) => Some(ver)
        case _ => None
      },
      gitDescribedVersion := gitTagToVersionNumber.value(
        com.typesafe.sbt.git.ConsoleGitRunner(
          "describe",
          "--match", "v[0-9]*.[0-9]*.[0-9]*" // this is a glob-pattern, not a regex
        )(baseDirectory.value)
      )
    )

}
