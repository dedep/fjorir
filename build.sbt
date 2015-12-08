name := """akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  // Change this to another test framework if you prefer
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.1"
  //"com.typesafe.akka" %% "akka-remote" % "2.3.5",
  //"com.typesafe.akka" %% "akka-testkit" % "2.3.5"
)
