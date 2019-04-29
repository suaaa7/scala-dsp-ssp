lazy val finagleVersion = "18.12.0"
lazy val finchVersion = "0.28.0"
lazy val genericVersion = "0.11.0"
lazy val configVersion = "1.3.4"

lazy val lib = Seq(
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "io.circe" %% "circe-generic" % genericVersion,
  "com.typesafe" % "config" % configVersion
)

lazy val root = (project in file("."))
  .settings(
    name := "ScalaSSP",
    version := "1.0.0",
    scalaVersion := "2.12.8",
    libraryDependencies ++= lib
  )
