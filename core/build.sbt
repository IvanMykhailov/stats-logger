name := "stats-logger-core"

organization := "me.singularex"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "com.typesafe.play" %% "play-json" % "2.2.1",
//  "com.ning" % "async-http-client" % "1.8.3",
  "com.typesafe" % "config" % "1.2.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.1.2"//,
//  "com.typesafe.akka" %% "akka-actor" % "2.2.1"
)

//MongoDb driver
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)



//Tests
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
  //"com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test"
)


scalacOptions ++= Seq(
    "-deprecation"
  , "-feature"
  , "-unchecked"
  , "-Xlint"
  , "-Yno-adapted-args"
  , "-Ywarn-dead-code"
  , "-language:postfixOps"
  , "-language:implicitConversions"
)

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

net.virtualvoid.sbt.graph.Plugin.graphSettings

//fork := true

//javaOptions := Seq("-Dconfig.resource=application.conf", "-Dakka.log-config-on-start=on")