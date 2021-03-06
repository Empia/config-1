import Dependencies._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtSite.SiteKeys._
import UnidocKeys._

lazy val projectName = "config"

lazy val commonSettings = Seq(
  moduleName := projectName,
  organization := "com.lambdista",
  scalaVersion := "2.12.0",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
  resolvers ++= Seq(Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots")),
  scalacOptions := Seq(
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-encoding",
    "utf8",
    "-deprecation",
    "-unchecked"
  ),
  scalafmtConfig := Some(file(".scalafmt.conf")),
  initialCommands in console :=
    """
      |import scala.util._
      |import scala.concurrent.duration._
      |import scala.concurrent.duration.Duration._
      |import com.lambdista.config._
    """.stripMargin
)

lazy val noPublishSettings = Seq(publish := (), publishLocal := (), publishArtifact := false)

lazy val config = (project in file("."))
  .enablePlugins(GitBranchPrompt)
  .aggregate(core, util, typesafe)
  .dependsOn(core, util, typesafe)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    moduleName := "config-root",
    (unmanagedSourceDirectories in Compile) := Nil,
    (unmanagedSourceDirectories in Test) := Nil
  )

lazy val core = (project in file("core"))
  .dependsOn(util)
  .settings(commonSettings)
  .settings(Publishing.settings)
  .settings(moduleName := projectName, libraryDependencies ++= coreDeps, dependencyOverrides += shapeless)

lazy val util = (project in file("util"))
  .settings(commonSettings)
  .settings(Publishing.settings)
  .settings(moduleName := "config-util")

lazy val typesafe = (project in file("typesafe"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(Publishing.settings)
  .settings(moduleName := "config-typesafe", libraryDependencies ++= typesafeDeps)

lazy val docSettings = tutSettings ++ site.settings ++ ghpages.settings ++ unidocSettings ++ Seq(
    site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "api"),
    ghpagesNoJekyll := false,
    git.remoteRepo := "https://github.com/lambdista/config",
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(util),
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.svg" | "*.js" | "*.swf" | "*.yml" | "*.md"
  )

lazy val docs = (project in file("docs"))
  .dependsOn(core, typesafe)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(docSettings)
  .settings(moduleName := "config-docs", tutSourceDirectory := file("docs/src/tut"), tutTargetDirectory := file("."))
