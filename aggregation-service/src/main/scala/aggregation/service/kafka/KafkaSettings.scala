package aggregation.service.kafka

import zio._
import aggregation.service.config.AppConfig
import zio.kafka.consumer.diagnostics.Diagnostics

object KafkaSettings {
  private val diagnostics: Diagnostics = Diagnostics.NoOp

  lazy val consumerSettingsLive = (for {
    appConfig <- ZIO.service[AppConfig]
  } yield appConfig.consumer.toConsumerSettings).toLayer ++ ZLayer.succeed(diagnostics)
}
