package enrichment.service.kafka

import zio._
import enrichment.service.config.AppConfig
import zio.kafka.consumer.diagnostics.Diagnostics

object KafkaSettings {
  lazy val producerSettingsLive = (for {
    appConfig <- ZIO.service[AppConfig]
  } yield appConfig.producer.toProducerSettings).toLayer

  private val diagnostics: Diagnostics = Diagnostics.NoOp

  lazy val consumerSettingsLive = (for {
    appConfig <- ZIO.service[AppConfig]
  } yield appConfig.consumer.toConsumerSettings).toLayer ++ ZLayer.succeed(diagnostics)
}
