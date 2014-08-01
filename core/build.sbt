name := "mall-locator-sensor"

organization := "me.singularex"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalaj" % "scalaj-time_2.10.2" % "0.7",
  "com.typesafe.play" %% "play-json" % "2.3.1",
//  "com.ning" % "async-http-client" % "1.8.3",
  "com.typesafe" % "config" % "1.2.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.1.2"//,
//  "com.typesafe.akka" %% "akka-actor" % "2.2.1"
)

MongoDb driver
libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
)



//Tests
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0" % "test"//,
  //"com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test"
)


scalacOptions ++= Seq(
    "-deprecation"
  , "-feature"
  , "-unchecked"
  , "-Xlint"
  , "-Yno-adapted-args"
  , "-Ywarn-all"
  , "-Ywarn-dead-code"
  , "-language:postfixOps"
  , "-language:implicitConversions"
)

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

net.virtualvoid.sbt.graph.Plugin.graphSettings

//fork := true

//javaOptions := Seq("-Dconfig.resource=application.conf", "-Dakka.log-config-on-start=on")