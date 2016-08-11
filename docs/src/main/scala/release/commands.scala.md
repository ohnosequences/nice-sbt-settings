
```scala
package ohnosequences.sbt.nice.release

import ohnosequences.sbt.nice._
import sbt._, Keys._, complete._, DefaultParsers._, CommandStrings._


case object commands {

  lazy val releaseCommand = Command("release")(releaseCommandArgsParser)(releaseCommandAction)

  def releaseCommandArgsParser(state: State): Parser[Version] = {
    val git = Git(Project.extract(state).get(baseDirectory), state.log)
    Space ~> parsers.versionBumperParser(git.version)
  }
```

This is the action of the release command. It cannot be a task, because after release preparation we need to reload the state to update the version setting.

```scala
  def releaseCommandAction(state: State, releaseVersion: Version): State = {

    implicit def keyAsInput(tk: Scoped): String = tk.key.label
    def spaced(strs: String*): String = strs.mkString(" ")

    // Here everything is converted to strings and prepended to remainingCommands of the state (it's the same if you manually entered those strings in the sbt console one by one)
    spaced(keys.prepareRelease, releaseVersion.toString) ::
    LoadProject :: // = reload
    spaced(keys.makeRelease, releaseVersion.toString) ::
    state
  }

}

```




[main/scala/AssemblySettings.scala]: ../AssemblySettings.scala.md
[main/scala/Git.scala]: ../Git.scala.md
[main/scala/JavaOnlySettings.scala]: ../JavaOnlySettings.scala.md
[main/scala/MetadataSettings.scala]: ../MetadataSettings.scala.md
[main/scala/package.scala]: ../package.scala.md
[main/scala/release/commands.scala]: commands.scala.md
[main/scala/release/keys.scala]: keys.scala.md
[main/scala/release/parsers.scala]: parsers.scala.md
[main/scala/release/tasks.scala]: tasks.scala.md
[main/scala/ReleasePlugin.scala]: ../ReleasePlugin.scala.md
[main/scala/ResolverSettings.scala]: ../ResolverSettings.scala.md
[main/scala/ScalaSettings.scala]: ../ScalaSettings.scala.md
[main/scala/StatikaBundleSettings.scala]: ../StatikaBundleSettings.scala.md
[main/scala/Version.scala]: ../Version.scala.md
[main/scala/VersionSettings.scala]: ../VersionSettings.scala.md
[main/scala/WartRemoverSettings.scala]: ../WartRemoverSettings.scala.md