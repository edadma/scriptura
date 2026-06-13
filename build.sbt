ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme      := Some("semver-spec")
ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalaVersion       := "3.8.4"

lazy val typesetter = ProjectRef(file("../typesetter"), "typesetterJVM")

lazy val typesetterNative = ProjectRef(file("../typesetter"), "typesetterNative")

lazy val suitNative = ProjectRef(file("../suit"), "suitNative")

lazy val scriptura = crossProject( /*JSPlatform,*/ JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
    name    := "scriptura",
    version := "0.0.1",
    scalacOptions ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:existentials",
        "-language:dynamics",
      ),
    organization           := "io.github.edadma",
    publishMavenStyle      := true,
    Test / publishArtifact := false,
    licenses += "ISC"      -> url("https://opensource.org/licenses/ISC"),
    libraryDependencies ++= Seq(
      "com.github.scopt" %%% "scopt"  % "4.1.0",
      "com.lihaoyi"      %%% "pprint" % "0.9.0",
    ),
    resolvers += "Sonatype OSS Releases" at "https://s01.oss.sonatype.org/content/repositories/releases",
  )
  .jvmConfigure(_.dependsOn(typesetter))
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0" % "test",
    ),
  )
  .nativeConfigure(_.dependsOn(typesetterNative, suitNative))
  .nativeSettings(
    // suit ships a demo `@main def main` in its native main sources, so its launcher lands on the
    // classpath alongside this app's CLI entry point; name the CLI explicitly so `run`/`nativeLink`
    // are unambiguous.
    Compile / mainClass := Some("io.github.edadma.scriptura.run"),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % Test,
  )
//  .jsSettings(
//    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
//    //    Test / scalaJSUseMainModuleInitializer := true,
//    //    Test / scalaJSUseTestModuleInitializer := false,
//    Test / scalaJSUseMainModuleInitializer := false,
//    Test / scalaJSUseTestModuleInitializer := true,
//    scalaJSUseMainModuleInitializer        := true,
//  )
