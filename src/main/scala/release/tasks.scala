package ohnosequences.sbt.nice.release

import ohnosequences.sbt.nice._

import sbt._, Keys._, complete._, DefaultParsers._
import ohnosequences.sbt.SbtGithubReleasePlugin.autoImport._
import VersionSettings.autoImport._
import com.markatta.sbttaglist.TagListPlugin._
import scala.collection.immutable.SortedSet
import org.kohsuke.github.GHRelease
import laughedelic.literator.plugin.LiteratorPlugin.autoImport._
import java.nio.file.Files
import AssemblySettings.autoImport._
import Git._


case object tasks {

  /* Asks user for the confirmation to continue */
  private def confirmContinue(msg: String): Unit = {

    SimpleReader.readLine(s"\n${msg} (y/n) ").map(_.toLowerCase) match {
      case Some("y") => {} // go on
      case Some("n") => sys.error("User chose to abort release process")
      case _ => {
        println("Didn't understand your answer. Try again.")
        confirmContinue(msg)
      }
    }
  }

  private def withConfirmation[T](msg: String)(taskDef: DefTask[T]): DefTask[T] = Def.taskDyn {
    confirmContinue(msg)
    taskDef
  }

  private def announce(msg: String): DefTask[Unit] = Def.task {
    val log = streams.value.log
    log.info("")
    log.info(msg)
    log.info("")
  }


  /* We try to check as much as possible _before_ making any release-related changes. If these checks are not passed, it doesn't make sense to start release process at all */
  def prepareRelease(releaseVersion: Version): DefTask[Unit] = Def.sequential(
    announce("Checking git repository..."),
    checkGit(releaseVersion),
    checkGithubCredentials,

    announce("Checking code notes..."),
    checkCodeNotes,

    announce("Checking project dependencies..."),
    checkDependencies,
    update,

    announce("Running non-release tests..."),
    clean,
    test.in(Test),

    announce("Preparing release notes and creating git tag..."),
    prepareReleaseNotesAndTag(releaseVersion)
  )


  def checkGit(releaseVersion: Version): DefTask[Unit] = Def.task {
    val log = streams.value.log
    val git = Git.task.value

    if (git.isDirty) {
      log.error("You have uncommited changes. Commit or stash them first.")
      sys.error("Git repository is not clean.")
    }

    val loaded = gitVersion.value
    val actual = git.version

    if (loaded != actual) {
      log.error(s"Current version ${loaded} is outdated (should be ${actual}). Reload and start release process again.")
      sys.error("Outdated version setting.")
    }

    // TODO: probably remote name should be configurable
    val remoteName = git.currentRemote.getOrElse(origin)

    log.info(s"Updating remote [${remoteName}].")
    if (git.silent.remote("update", remoteName).exitCode != 0) {
      log.error(s"Remote [${remoteName}] is not set or is not accessible. Check your git-config or internet connection.")
      sys.error("Remote repository is not accessible.")
    }

    val tagName = "v" + releaseVersion
    if (git.silent.tagList(tagName) contains tagName) {
      log.error(s"Git tag ${tagName} already exists. You cannot release this version.")
      sys.error("Git tag already exists.")
    }

    val current:  String = git.currentBranch.getOrElse(HEAD)
    val upstream: String = git.currentUpstream.getOrElse {
      sys.error("Couldn't get current branch upstream.")
    }
    val commitsBehind: Int = git.commitsNumber(s"${current}..${upstream}").getOrElse {
      sys.error("Couldn't compare current branch with its upstream.")
    }

    if (commitsBehind > 0) {
      log.error(s"Local branch [${current}] is ${commitsBehind} commits behind [${upstream}]. You need to pull the changes.")
      sys.error("Local branch state is outdated.")
    } else {
      log.info(s"Local branch [${current}] is up to date with its remote upstream.")
    }
  }

  def checkCodeNotes: DefTask[Unit] = Def.task {
    // NOTE: this task outputs the list
    val list = TagListKeys.tagList.value
    if (list.flatMap{ _._2 }.nonEmpty) {
      confirmContinue("Are you sure you want to continue without fixing it?")
    }
  }

  /* Returns the list of dependencies with changing/snapshot versions */
  def snapshotDependencies: DefTask[Seq[ModuleID]] = Def.task {

    libraryDependencies.value.filter { mod =>
      mod.isChanging ||
      mod.revision.endsWith("-SNAPSHOT")
    }
  }


  /* Almost the same as the task `dependencyUpdates`, but it outputs result as a warning
     and asks for a confirmation if needed */
  def checkDependencies: DefTask[Unit] = Def.taskDyn {
    import com.timushev.sbt.updates._, versions.{ Version => UpdVer }, UpdatesKeys._
    val log = streams.value.log

    val snapshots: Seq[ModuleID] = snapshotDependencies.value

    if (snapshots.nonEmpty) {
      log.error(s"You cannot start release process with snapshot dependencies:")
      snapshots.foreach { mod => log.error(s" - ${mod}") }
      log.error("Update dependencies, commit and run release process again.")
      sys.error("Project has unstable dependencies.")

    } else Def.task {

      val updatesData: Map[ModuleID, SortedSet[UpdVer]] = dependencyUpdatesData.value

      if (updatesData.nonEmpty) {
        log.warn( Reporter.dependencyUpdatesReport(projectID.value, updatesData) )
        confirmContinue("Are you sure you want to continue with outdated dependencies?")

      } else log.info("All dependencies seem to be up to date.")
    }
  }


  /* This generates scalatest tags for marking tests (for now just release-only tests) */
  def generateTestTags: DefTask[Seq[File]] = Def.task {
    val file = sourceManaged.in(Test).value / "test" / "releaseOnlyTag.scala"

    lazy val parts = keys.releaseOnlyTestTag.value.split('.')
    lazy val pkg = parts.init.mkString(".")
    lazy val obj = parts.last

    IO.write(file, s"""
      |package ${pkg}
      |
      |case object ${obj} extends org.scalatest.Tag("${pkg}.${obj}")
      |""".stripMargin
    )

    Seq(file)
  }


  /* This task checks the precense of release notes file and returns either
     - Left[File] if the the file needs to be renamed
     - Right[File] otherwise
  */
  def checkReleaseNotes(releaseVersion: Version): DefTask[Either[File, File]] = Def.task {
    val log = streams.value.log

    val notesDir = baseDirectory.value / "notes"

    // TODO: these could be configurable
    val alternativeNames     = Set("Changelog", "Next")
    val acceptableExtensions = Set("markdown", "md")

    val notesFinder: PathFinder = (notesDir * "*") filter { file =>
      (acceptableExtensions contains file.ext) && (
        (file.base == releaseVersion.toString) ||
        (alternativeNames.map(_.toLowerCase) contains file.base.toLowerCase)
      )
    }

    val finalMessage = "Write release notes, commit and run release process again."

    notesFinder.get match {
      case Nil => {
        val acceptableNames = {
          alternativeNames.map(_+".md") +
          s"${releaseVersion}.markdown"
        }
        log.error(s"""No release notes found. Place them in the notes/ directory with one of the following names: ${acceptableNames.mkString("'", "', '", "'")}.""")
        log.error(finalMessage)
        sys.error("Absent release notes.")
      }

      case Seq(notesFile) => {
        val notes = IO.read(notesFile)
        val notesPath = notesFile.relativeTo(baseDirectory.value).getOrElse(notesFile.getPath)

        if (notes.isEmpty) {
          log.error(s"Notes file [${notesPath}] is empty.")
          log.error(finalMessage)
          sys.error("Empty release notes.")

        } else {
          log.info(s"Taking release notes from the [${notesPath}] file:")
          println(notes)

          confirmContinue("Do you want to proceed with these release notes?")

          if (notesFile.base == releaseVersion) Right(notesFile)
          else Left(notesFile)
        }
      }

      case multipleFiles => {
        log.error("You have several release notes files:")
        multipleFiles.foreach { f => log.error(s" - notes/${f.name}") }
        log.error("Please, leave only one of them, commit and run release process again.")
        sys.error("Multiple release notes.")
      }
    }
  }

  def prepareReleaseNotesAndTag(releaseVersion: Version): DefTask[Unit] = Def.task {
    val log = streams.value.log
    val git = Git.task.value

    // Either take the version-named file or rename the changelog-file and commit it
    val notesFile = checkReleaseNotes(releaseVersion).value match {
      case Right(file) => file
      case Left(changelogFile) => {
        val versionFile = baseDirectory.value / "notes" / s"${releaseVersion}.markdown"
        git.mv(changelogFile, versionFile)
        git.stageAndCommit(s"Release notes for v${releaseVersion}")(changelogFile, versionFile)
        versionFile
      }
    }
    // TODO: (optionally) symlink notes/latest.md (useful for bintray)

    git.createTag(notesFile, releaseVersion)
    log.info(s"Created git tag [v${releaseVersion}].")
  }


  /* After release is prepared this sequence is going to actually make the release (publish, etc.) */
  def makeRelease(releaseVersion: Version): DefTask[Unit] = Def.taskDyn {
    val log = streams.value.log
    val git = Git.task.value

    if (git.version != releaseVersion) {
      log.error(s"This task should be run after ${keys.prepareRelease.key.label} and reload.")
      log.error(s" Versions don't coincide: git version is [${git.version}], should be [${releaseVersion}].")
      sys.error("Outdated version setting.")
    }

    Def.sequential(
      announce("Publishing release artifacts..."),
      publish.in(keys.Release),

      announce("Running release tests..."),
      test.in(keys.Release),

      announce("Publishing release on Github..."),
      pushHeadAndTag,
      releaseOnGithub,

      announce("Release has successfully finished!"),

      withConfirmation(
        "Do you want to generate and publish Literator source docs?"
      )(publishLiteratorDocs),

      withConfirmation(
        "Do you want to generate and publish API docs to gh-pages?"
      )(publishApiDocs(latest = true))
    )
  }


  /* This task pushes current branch and tag to the remote */
  def pushHeadAndTag: DefTask[Unit] = Def.task {
    val git = Git.task.value
    val tagName = "v" + git.version
    val remoteName = git.currentRemote.getOrElse(origin)
    // We call .get on Try to fail the task if push was unsuccessful
    git.push(remoteName)(HEAD, tagName).critical
  }

  def publishRelease: DefTask[Unit] = Def.taskDyn {
    publish.value

    if (keys.publishFatArtifact.in(keys.Release).value)
      Def.task { fatArtifactUpload.value }
    else
      Def.task { streams.value.log.info("Skipping fat-jar publishing.") }
  }

  /* Cleans previously generated docs and re-generates them again */
  def publishLiteratorDocs: DefTask[Unit] = Def.taskDyn {

    val outputDir = docsOutputDirs.value
    Defaults.doClean(outputDir, Seq())

    Def.task {
      val git = Git.task.value
      generateDocs.value

      // We call .get on Try to fail the task if push was unsuccessful
      git.stageAndCommit(s"Autogenerated literator docs for v${gitVersionT.value}")(outputDir: _*).critical
      val remoteName = git.currentRemote.getOrElse(origin)
      git.push(remoteName)(HEAD).critical
    }
  }

  /* This task
     - clones `gh-pages` branch in a temporary directory
     - generates api docs with the standard sbt task `docs`
     - copies it to `docs/api/<version>`
     - symlinks `docs/api/latest` to it
     - commits and pushes `gh-pages` branch
  */
  // TODO: destination (gh-pages) could be configurable, probably with a help of sbt-site
  def publishApiDocs(latest: Boolean): DefTask[Unit] = Def.taskDyn {
    val log = streams.value.log
    val git = Git.task.value

    val remoteName = git.currentRemote.getOrElse(origin)
    val url = git.remoteUrl(remoteName).getOrElse {
      sys.error(s"Couldn't get remote [${remoteName}] url")
    }

    val ghpagesDir = IO.createTemporaryDirectory
    val gh_pages = "gh-pages"

    log.info(s"Cloning gh-pages branch in a temporary directory ${ghpagesDir}")
    if (git.silent.clone("--branch", gh_pages, "--single-branch", url, ghpagesDir.getPath).exitCode != 0) {
      log.error("Couldn't clone gh-pages branch, probably this repo doesn't have it yet.")
      log.error(s"Check it and rerun the [${keys.publishApiDocs.key.label}] command.")
      sys.error("gh-pages branch doesn't exist")
      // TODO: create it if it doesn't exist (don't forget --orphan)
      // add .nojekyll in the root for symlinks to work (see https://github.com/isaacs/github/issues/553)

    } else Def.task {
      val docTarget = doc.in(Compile).value

      val destBase   = ghpagesDir / "docs" / "api"
      val destVer    = destBase / version.value

      log.debug(s"Copying ${docTarget} to ${destVer}")
      if (destVer.exists) IO.delete(destVer)
      IO.copyDirectory(docTarget, destVer, overwrite = true)

      val destLatestOpt: Option[File] = if (latest) Some(destBase / "latest") else None

      destLatestOpt.foreach { destLatest =>
        log.debug(s"Symlinking ${destLatest} to ${destVer}")
        Files.deleteIfExists(destLatest.absPath)
        Files.createSymbolicLink(destLatest.absPath, destVer.relPath(destLatest.getParentFile))
      }

      log.info("Publishing API docs...")
      val ghpagesGit = Git(ghpagesDir, streams.value.log)

      val files = destVer +: destLatestOpt.toSeq
      ghpagesGit.stageAndCommit(s"API docs v${git.version}")(files: _*).critical
      ghpagesGit.push(remoteName)(gh_pages).critical
    }
  }

}
