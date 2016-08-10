package ohnosequences.sbt

import sbt._

package object nice {

  type DefTask[X] = Def.Initialize[Task[X]]
}
