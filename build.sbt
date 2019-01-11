name := "streaming-platform"

version := "0.1"

scalaVersion := "2.12.8"

//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.8"
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8"
//dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.8"
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.8"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "org.apache.spark" %% "spark-streaming" % "2.4.0",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0",
  "org.apache.kafka" %% "kafka" % "2.1.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.5.0",
  "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.0",
  "org.apache.spark" %% "spark-sql" % "2.4.0"

//  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.8",
//  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.8",
//  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.9.8"
  //"com.fasterxml.jackson.core" % "jackson-annotations" % "2.9.8"

)