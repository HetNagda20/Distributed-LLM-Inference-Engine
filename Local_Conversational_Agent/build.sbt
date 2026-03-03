ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "Local_Conversational_Agent"
  )
Compile / mainClass := Some("com.HetNagda.server.Server")
libraryDependencies ++= Seq(
  "io.github.ollama4j" % "ollama4j" % "1.0.79",
  "com.softwaremill.sttp.client3" %% "core" % "3.8.13",
  "com.softwaremill.sttp.client3" %% "play-json" % "3.8.13",
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",
  "com.typesafe.akka" %% "akka-stream" % "2.8.0",
  "org.mockito" %% "mockito-scala" % "1.17.12" % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % Test
//libraryDependencies += "org.scalatestplus" %% "play" % "5.1.0" % Test

// Add assembly plugin settings if you want to create a fat JAR
assembly / assemblyMergeStrategy := {
  case x if x.contains("META-INF/MANIFEST.MF") => MergeStrategy.discard
  case x if x.contains("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}