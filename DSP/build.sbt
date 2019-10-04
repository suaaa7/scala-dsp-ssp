lazy val finagleVersion = "18.12.0"
lazy val finchVersion = "0.28.0"
lazy val circeVersion = "0.11.1"
lazy val configVersion = "1.3.4"
lazy val catsVersion = "1.6.0"

lazy val lib = Seq(
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "util-cache-guava" % "18.12.0",
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe" % "config" % configVersion,
  "org.typelevel" %% "cats-core" % catsVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "scaladsp",
    version := "1.0.0",
    scalaVersion := "2.12.8",
    libraryDependencies ++= lib
  )

scalafmtVersion in ThisBuild := "1.5.1"
scalafmtTestOnCompile in ThisBuild := true
scalafmtShowDiff in (ThisBuild, scalafmt) := true
