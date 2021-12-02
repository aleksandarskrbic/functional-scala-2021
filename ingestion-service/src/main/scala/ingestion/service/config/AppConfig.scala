package ingestion.service.config

import zio._
import zio.config.read
import com.typesafe.config.ConfigFactory
import zio.config.typesafe.TypesafeConfigSource
import zio.config.magnolia.DeriveConfigDescriptor
import zio.kafka.producer.ProducerSettings

case class AppConfig(http: AppConfig.Http, producer: AppConfig.Producer)

object AppConfig {
  private val descriptor = DeriveConfigDescriptor.descriptor[AppConfig]

  case class Http(port: Int)
  case class Producer(bootstrapServers: String, topic: String) {
    def toProducerSettings: ProducerSettings = ProducerSettings(
      bootstrapServers.split(",").toList
    )
  }

  lazy val live = (for {
    rawConfig <- ZIO.effect(
      ConfigFactory.load().getConfig("ingestion-service")
    )
    configSource <- ZIO.fromEither(
      TypesafeConfigSource.fromTypesafeConfig(rawConfig)
    )
    config <- ZIO.fromEither(read(descriptor.from(configSource)))
  } yield config).toLayer.orDie
}
