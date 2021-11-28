import sbt._

object Dependencies {

  private object Versions {
    val zio = "1.0.12"
    val zioKafka = "0.17.1"
    val zioConfig = "1.0.10"
    val zioLogging = "0.5.13"
    val zioJson = "0.1.5"
    val zioHttp = "1.0.0.0-RC17"
    val sttp = "3.3.16"
    val log4j = "2.14.1"
    val disruptor = "3.4.4"
    val jackson = "2.13.0"
    val kafka = "3.0.0"
  }
  object Libraries {
    val zio = Seq(
      "dev.zio" %% "zio" % Versions.zio,
      "dev.zio" %% "zio-streams" % Versions.zio,
      "io.github.kitlangton" %% "zio-magic" % "0.3.11"
    )

    val zioKafka = Seq("dev.zio" %% "zio-kafka" % Versions.zioKafka)

    val zioHttp = Seq("io.d11" %% "zhttp" % Versions.zioHttp)

    val zioConfig = Seq(
      "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
      "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
    )

    val zioLogging = Seq(
      "dev.zio" %% "zio-logging" % Versions.zioLogging,
      "dev.zio" %% "zio-logging-slf4j" % Versions.zioLogging
    )

    val zioJson = Seq("dev.zio" %% "zio-json" % Versions.zioJson)

    val sttp = Seq(
      "com.softwaremill.sttp.client3" %% "httpclient-backend-zio" % Versions.sttp,
      "com.softwaremill.sttp.client3" %% "zio-json" % Versions.sttp
    )

    val logging = Seq(
      "org.apache.logging.log4j" % "log4j-core" % Versions.log4j,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j,
      "com.lmax" % "disruptor" % Versions.disruptor
    )

    val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson
    )
    val embeddedKafka = Seq(
      "io.github.embeddedkafka" %% "embedded-kafka" % Versions.kafka
    )
  }
}
