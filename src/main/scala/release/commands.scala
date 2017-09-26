package ohnosequences.sbt.nice.release

import ohnosequences.sbt.nice._
import sbt._, Keys._, complete._, DefaultParsers._, internal.CommandStrings._


case object commands {

  lazy val releaseCommand = Command("release")(releaseCommandArgsParser)(releaseCommandAction)

  def releaseCommandArgsParser(state: State): Parser[Version] = {
    val git = Git(Project.extract(state).get(baseDirectory), state.log)
    Space ~> parsers.versionBumperParser(git.version)
  }

  /* This is the action of the release command. It cannot be a task, because after release preparation we need to reload the state to update the version setting. */
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
