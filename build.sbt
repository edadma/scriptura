ThisBuild / organization  := "io.github.edadma"
ThisBuild / version       := "0.0.1"
ThisBuild / scalaVersion  := "3.5.0"
ThisBuild / licenses      += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

publish / skip := true

enablePlugins(ScalaNativePlugin)

lazy val root = (project in file("."))
  .dependsOn(compositor, texish) // typesetter is included transitively through compositor
  .settings(
    name := "io/github/edadma/scriptura", // Root project generates the executable
    libraryDependencies ++= Seq(
      "com.github.scopt" %%% "scopt" % "4.1.0",
      "com.lihaoyi" %%% "pprint" % "0.9.0",
    )
  )

lazy val compositor = (project in file("compositor"))
  .dependsOn(typesetter) // compositor explicitly depends on typesetter
  .settings(
    name := "compositor",
    libraryDependencies ++= Seq(
      //"io.github.edadma" %%% "libcairo" % "0.0.7", // Your custom Cairo facade
      "com.lihaoyi" %%% "pprint" % "0.9.0",
    ),
    githubOwner := "edadma",
    githubRepository := name.value,
  )

lazy val texish = (project in file("texish"))
  .settings(
    name := "texish",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.19" % "test"
  )

lazy val typesetter = (project in file("typesetter"))
  .settings(
    name := "typesetter",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19" % "test",
      "com.lihaoyi" %%% "pprint" % "0.9.0",
    )
  )
 
