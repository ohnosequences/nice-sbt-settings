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

    import ScalaSettings._
    import ResolverSettings._
    import DocumentationSettings._
    import TagListSettings._
    import ReleaseSettings._

    /* You can just say somewhere **in the very beginning** of your `build.sbt`:

       ```scala
       Nice.scalaProject

       // and then you can adjust any settings
       ```
    */
    lazy val scalaProject: Seq[Setting[_]] =
      metainfoSettings ++
      scalaSettings ++
      resolverSettings ++
      documentationSettings ++
      releaseSettings ++
      tagListSettings


    import JavaSettings._

    /* Same for `Nice.javaProject` — it includes all `scalaProject` settings */
    lazy val javaProject: Seq[Setting[_]] =
      scalaProject ++
      javaSettings

  }

}
