ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "EC2_Server",

    // Protobuf compilation setup for ScalaPB
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value
    ),

    // Dependencies
    libraryDependencies ++= Seq(
      // Akka HTTP and Akka Streams
      "com.typesafe.akka" %% "akka-http" % "10.5.0",
      "com.typesafe.akka" %% "akka-stream" % "2.8.0",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0", // for spray-json integration
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % "10.5.0" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.8.0" % Test,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf", // for protobuf

      // JSON serialization support
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0", // for JSON support with ScalaPB
      "com.softwaremill.sttp.client3" %% "core" % "3.8.13", // STTP client for HTTP requests
      "com.softwaremill.sttp.client3" %% "spray-json" % "3.8.13", // STTP with Spray-JSON integration
      "org.json4s" %% "json4s-native" % "4.0.6", // for JSON parsing and rendering

      // Logging (Logback for Akka)
      "ch.qos.logback" % "logback-classic" % "1.2.11" ,// Logging with Logback

      // ScalaTest for testing
      "org.scalatest" %% "scalatest" % "3.2.15" % Test // Test dependencies for unit tests
    ),


    // Optional: If using Akka TestKit for Akka HTTP testing, add this to the `Test` scope
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-testkit" % "10.5.0" % Test // For HTTP route testing in Akka
    ),
      assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x if x.endsWith("/module-info.class") => MergeStrategy.discard
      case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
      case "google/protobuf/struct.proto" => MergeStrategy.first
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x if x.contains("sigar") => MergeStrategy.first
      case x if x.contains("io.netty.versions.properties") => MergeStrategy.first
      case x if x.contains("module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
  )
