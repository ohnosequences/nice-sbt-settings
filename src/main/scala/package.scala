package ohnosequences.sbt

import sbt._
import java.nio.file.Path

package object nice {

  type DefTask[X] = Def.Initialize[Task[X]]

  implicit class FileOps(val file: File) extends AnyVal {

    def absPath: Path = file.toPath.toAbsolutePath.normalize
    def relPath(base: File): Path = base.absPath relativize file.absPath
  }
}
