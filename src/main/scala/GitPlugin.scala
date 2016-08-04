package ohnosequences.sbt.nice

import sbt._, Keys._

case object GitPlugin extends sbt.AutoPlugin {

  override def trigger = allRequirements
  override def requires = empty

  case object autoImport {

    lazy val gitTask = taskKey[GitRunner]("Git runner instance with streams logging")
  }
  import autoImport._

  override def projectSettings: Seq[Setting[_]] = Seq(
    gitTask := GitRunner(baseDirectory.value, streams.value.log)
  )
}
