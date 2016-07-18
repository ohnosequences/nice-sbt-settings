/* ## Project configurations

   This is the module defining project configurations, which are
   just combinations of the setting sets defined in other modules.
*/
package ohnosequences.sbt.nice

import sbt._
import Keys._
import sbt.Extracted

object NiceProjectConfigs extends sbt.Plugin {

  object Nice {

    /* You can just say somewhere **in the very beginning** of your `build.sbt`:

       ```scala
       Nice.scalaProject

       // and then you can adjust any settings
       ```
    */
    lazy val scalaProject: Seq[Setting[_]] =
      ReleaseSettings.releaseSettings ++
      WartremoverSettings.wartremoverSettings

  }

}
