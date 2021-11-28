import Dependencies.Libraries._

name         := "functional-scala-2021"
version      := "0.1.0"
scalaVersion := "2.13.7"
cancelable   := true

lazy val `embedded-kafka` = project
  .in(file("embedded-kafka"))
  .settings(
    libraryDependencies ++= embeddedKafka ++ logging
  )

lazy val protocol = project
  .in(file("protocol"))
  .settings(libraryDependencies ++= zioJson)

lazy val `aggregation-service` = project
  .in(file("aggregation-service"))
  .dependsOn(protocol)
  .settings(
    libraryDependencies ++=
      zio ++ zioKafka ++ zioHttp ++ zioLogging ++ logging ++ zioConfig ++ zioJson ++ jackson
  )

lazy val `enrichment-service` = project
  .in(file("enrichment-service"))
  .dependsOn(protocol)
  .settings(
    libraryDependencies ++=
      zio ++ zioKafka ++ zioHttp ++ zioLogging ++ logging ++ zioConfig ++ zioJson ++ jackson
  )

lazy val `ingestion-service` = project
  .in(file("ingestion-service"))
  .dependsOn(protocol)
  .settings(
    libraryDependencies ++=
      zio ++ zioKafka ++ zioHttp ++ zioLogging ++ logging ++ zioConfig ++ zioJson ++ jackson
  )

lazy val `query-service` = project
  .in(file("query-service"))
  .dependsOn(protocol)
  .settings(
    libraryDependencies ++=
      zio ++ zioHttp ++ zioLogging ++ logging ++ zioConfig ++ zioJson
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
