name := """akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-actor" % "2.3.12",
  "com.github.ironfish" %  "akka-persistence-mongo-casbah_2.11" % "0.7.6",
  "org.apache.commons"  %  "commons-math3" % "3.5"
)
