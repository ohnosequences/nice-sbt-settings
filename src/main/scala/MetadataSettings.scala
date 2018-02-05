/* ## Project metadata settings

   This module defines some [sbt project metadata](http://www.scala-sbt.org/release/docs/Howto/metadata.html):
   default homepages and the license.
*/
package ohnosequences.sbt.nice

import sbt._, Keys._

case object MetadataSettings extends sbt.AutoPlugin {

  override def requires = plugins.CorePlugin
  override def trigger = allRequirements

  /* ### Settings */
  override def projectSettings: Seq[Setting[_]] = Seq(
    homepage := Some(url("https://github.com/"+organization.value+"/"+name.value)),
    organizationHomepage := Some(url("http://"+organization.value+".com")),
    licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
  )

}
