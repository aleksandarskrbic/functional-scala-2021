name         := "functional-scala-2021"
version      := "0.1.0"
scalaVersion := "2.13.7"

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

lazy val `aggregation-service` = project.in(file("aggregation-service"))
lazy val `enrichment-service`  = project.in(file("enrichment-service"))
lazy val `ingestion-service`   = project.in(file("ingestion-service"))
lazy val `query-service`       = project.in(file("query-service"))
