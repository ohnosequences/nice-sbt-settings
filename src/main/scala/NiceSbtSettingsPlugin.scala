package ohnosequences.sbt

import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

import ohnosequences.sbt.SbtS3Resolver._

import sbtassembly._
import sbtassembly.Plugin._
import AssemblyKeys._

object NiceSettingsPlugin extends sbt.Plugin {

  // Setting keys:
  lazy val isPrivate = settingKey[Boolean]("If true, publish to private S3 bucket, else to public")
  lazy val bucketSuffix = settingKey[String]("Amazon S3 bucket suffix for resolvers")
  lazy val publishBucketSuffix = settingKey[String]("Amazon S3 bucket suffix for publish-to resolver")
  lazy val publishS3Resolver = settingKey[S3Resolver]("S3Resolver which will be used in publishTo")
  lazy val fatArtifactClassifier = settingKey[String]("Classifier of the fat jar artifact")

  lazy val docsInputDir = settingKey[String]("Directory with the documented sources")
  lazy val docsOutputDir = settingKey[String]("Output directory for the generated documentation")
  lazy val generateDocs = taskKey[Unit]("Generates markdown docs from code using literator tool")

  // Just some aliases for the patterns
  val mvn = Resolver.mavenStylePatterns
  val ivy = Resolver.ivyStylePatterns

  object Nice {

    // Sets of settings:
    lazy val metainfoSettings: Seq[Setting[_]] = Seq(
        homepage := Some(url("https://github.com/"+organization.value+"/"+name.value))
      , organizationHomepage := Some(url("http://"+organization.value+".com"))
      , licenses := Seq("AGPL-V3" -> url("http://www.gnu.org/licenses/agpl-3.0.txt"))
      )

    lazy val scalaSettings: Seq[Setting[_]] = Seq(
      // this doesn't allow any conflicts in dependencies:
        conflictManager := ConflictManager.strict

      , scalaVersion := "2.10.3"
      // 2.10.x are compatible and we want to use the latest _for everything_:
      , dependencyOverrides += "org.scala-lang" % "scala-library" % scalaVersion.value
      , dependencyOverrides += "org.scala-lang" % "scala-reflect" % scalaVersion.value

      , scalacOptions ++= Seq(
            "-feature"
          , "-language:higherKinds"
          , "-language:implicitConversions"
          , "-language:postfixOps"
          , "-deprecation"
          , "-unchecked"
          , "-Xlint"
          , "-target:jvm-1.7"
          )
      )

    lazy val javaSettings: Seq[Setting[_]] = Seq(
      // to omit _2.10 suffixes:
        crossPaths := false
      // to omit scala library dependency
      , autoScalaLibrary := false

      , javacOptions ++= Seq(
          "-source", "1.7"
        , "-target", "1.7"
        , "-Xlint:unchecked"
        , "-encoding", "UTF-8"
        )
      )

    lazy val resolversSettings: Seq[Setting[_]] = Seq(
        bucketSuffix := {organization.value + ".com"}
      , resolvers ++= Seq ( 
          organization.value + " public maven releases"  at 
            toHttp("s3://releases." + bucketSuffix.value)
        , organization.value + " public maven snapshots" at 
            toHttp("s3://snapshots." + bucketSuffix.value)
        // ivy
        , Resolver.url(organization.value + " public ivy releases", 
                       url(toHttp("s3://releases." + bucketSuffix.value)))(ivy)
        , Resolver.url(organization.value + " public ivy snapshots", 
                       url(toHttp("s3://snapshots." + bucketSuffix.value)))(ivy)
        ) 
      )

    lazy val publishingSettings: Seq[Setting[_]] = Seq(
        isPrivate := false
      , publishMavenStyle := true
      , publishBucketSuffix := bucketSuffix.value
      , publishS3Resolver := {
          val privacy = if (isPrivate.value) "private." else ""
          val prefix = if (isSnapshot.value) "snapshots" else "releases"
          val address = privacy+prefix+"."+publishBucketSuffix.value 
          S3Resolver( 
            name = address+" S3 publishing bucket"
          , url = "s3://"+address
          , patterns = if(publishMavenStyle.value) mvn else ivy
          , overwrite = isSnapshot.value
          )
        }
      , publishTo := {s3credentials.value map publishS3Resolver.value.toSbtResolver}

      // disable publishing docs
      , publishArtifact in (Compile, packageDoc) := false
      )

    lazy val fatArtifactSettings: Seq[Setting[_]] =
      (assemblySettings: Seq[Setting[_]]) ++ 
      addArtifact(artifact in (Compile, assembly), assembly) ++ Seq(
      // publishing also a fat artifact:
        fatArtifactClassifier := "fat"
      ,  artifact in (Compile, assembly) :=
        (artifact in (Compile, assembly)).value.copy(
           `classifier` = Some(fatArtifactClassifier.value)
        )
      , test in assembly := {}
      )

    lazy val literatorSettings: Seq[Setting[_]] = Seq(
      docsInputDir := sourceDirectory.value.toString
    , docsOutputDir := "docs/src/"
    , generateDocs := {
        val s: TaskStreams = streams.value
        s.log.info("Generating documentation...")

        val errors = ohnosequences.tools.Literator.literateDir(
                      new File(docsInputDir.value), Some(new File(docsOutputDir.value)))
        errors foreach { s.log.error(_) }

        if (errors.nonEmpty) sys.error("Couldn't generate documantation due to parsing errors")
        else s.log.info("Documentation is written to " + docsOutputDir.value)
      }
    )

    lazy val checkReleaseNotes: ReleaseStep = { st: State =>
      val extracted = Project.extract(st)
      val v: String = st.get(versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))._1.toString
      val base: File = extracted.get(baseDirectory)
      val note: File = base / "notes" / (v+".markdown")
      lazy val err = sys.error("You forgot to write release notes for "+v+" version")
      if (!note.exists) { st.log.error("File "+note+" doesn't exist"); err }
      else {
        val text: String = IO.read(note)
        if (text.isEmpty) { st.log.warn("File "+note+" is empty"); err }
        else {
          st.log.info("Release notes for v"+v+":\n------\n"+text+"\n------")
          SimpleReader.readLine("Do you want to stop release and edit these notes (y/n)? [n] ") match {
            case Some("y" | "Y") => sys.error("Aborting release. Go write better release notes.")
            case _ => // nothing happens;
          }
        }
      }
      st
    }

    lazy val genDocsForRelease: ReleaseStep = 
      ReleaseStep({st => Project.extract(st).runTask(generateDocs, st)._1 })

    lazy val checkDependencyUpdates: ReleaseStep = {
      import com.timushev.sbt.updates._
      import UpdatesKeys._
      ReleaseStep({st => Project.extract(st).runTask(dependencyUpdates, st)._1 })
    }

    lazy val releaseSettings: Seq[Setting[_]] = 
      ReleasePlugin.releaseSettings ++ Seq(
        versionBump := Version.Bump.Minor
      , tagComment  := {name.value + " v" + (version in ThisBuild).value}
      , releaseProcess := // use thisProjectRef.value if needed
          Seq[ReleaseStep](
            genDocsForRelease // <--
          , checkSnapshotDependencies
          , inquireVersions
          , checkReleaseNotes  // <--
          , runTest
          , setReleaseVersion
          , commitReleaseVersion
          , tagRelease
          , publishArtifacts
          , setNextVersion
          , pushChanges
          )
        }
      )

    // Global combinations of settings:
    lazy val scalaProject: Seq[Setting[_]] =
      metainfoSettings ++
      literatorSettings ++
      scalaSettings ++
      resolversSettings ++
      publishingSettings ++
      releaseSettings

    lazy val javaProject: Seq[Setting[_]] =
      scalaProject ++
      javaSettings

  }

}
