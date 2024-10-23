ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

lazy val scriptura = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file(".")).
  settings(
    name := "scriptura",
    version := "0.0.1",
    scalaVersion := "3.5.2",
    scalacOptions ++=
      Seq(
        "-deprecation", "-feature", "-unchecked",
        "-language:postfixOps", "-language:implicitConversions", "-language:existentials", "-language:dynamics",
        "-Xasync"
      ),
    organization := "io.github.edadma",
    githubOwner := "edadma",
    githubRepository := name.value,
    publishMavenStyle := true,
    Test / publishArtifact := false,
    licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
  ).
  jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
  ).
  nativeSettings(
  ).
  jsSettings(
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    //    Test / scalaJSUseMainModuleInitializer := true,
    //    Test / scalaJSUseTestModuleInitializer := false,
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer := true,
  )




ThisBuild / organization := "io.github.edadma"
ThisBuild / version := "0.0.1"
ThisBuild / scalaVersion := "3.5.0"
ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / githubOwner := "edadma"
ThisBuild / githubRepository := name.value

//ThisBuild / trackInternalDependencies := TrackLevel.TrackIfMissing

publish / skip := true

//enablePlugins(ScalaNativePlugin)

lazy val root = (project in file("."))
  .dependsOn(typesetter, texish)
  .settings(
    name := "scriptura",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.1.0",
      "com.lihaoyi" %% "pprint" % "0.9.0",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0" % "test",
    ),
  )

lazy val texish = (project in file("texish"))
  .settings(
    name := "texish",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "io.github.edadma" %% "char-reader" % "0.1.12",
      "com.lihaoyi" %% "pprint" % "0.9.0",
    ),
  )

lazy val typesetter = (project in file("typesetter"))
  .settings(
    name := "typesetter",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "com.lihaoyi" %% "pprint" % "0.9.0",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0" % "test",
    ),
  )
