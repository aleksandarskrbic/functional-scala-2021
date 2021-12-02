package ingestion.service.kafka

import zio._
import ingestion.service.config.AppConfig

object EventProducerSettings {
  lazy val live = (for {
    appConfig <- ZIO.service[AppConfig]
  } yield appConfig.producer.toProducerSettings).toLayer
}
