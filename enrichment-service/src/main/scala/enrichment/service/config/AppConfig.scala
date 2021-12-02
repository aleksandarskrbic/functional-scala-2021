package enrichment.service.config

import zio._
import zio.config.read
import com.typesafe.config.ConfigFactory
import zio.config.typesafe.TypesafeConfigSource
import zio.config.magnolia.DeriveConfigDescriptor
import zio.duration.Duration
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings
import scala.concurrent.duration.DurationInt

case class AppConfig(
    consumer: AppConfig.Consumer,
    producer: AppConfig.Producer,
    enrichment: AppConfig.Enrichment
)

object AppConfig {
  private val descriptor = DeriveConfigDescriptor.descriptor[AppConfig]

  case class Consumer(bootstrapServers: String, topic: String, groupId: String) {
    def toConsumerSettings: ConsumerSettings =
      ConsumerSettings(bootstrapServers.split(",").toList)
        .withGroupId(groupId)
        .withClientId("client")
        .withCloseTimeout(Duration.fromScala(30.seconds))
        .withPollTimeout(Duration.fromMillis(10))
        .withProperty("enable.auto.commit", "false")
        .withProperty("auto.offset.reset", "earliest")
  }

  case class Producer(bootstrapServers: String, topic: String) {
    def toProducerSettings: ProducerSettings = ProducerSettings(
      bootstrapServers.split(",").toList
    )
  }

  case class Enrichment(url: String)

  lazy val live = (for {
    rawConfig <- ZIO.effect(ConfigFactory.load().getConfig("enrichment-service"))
    configSource <- ZIO.fromEither(TypesafeConfigSource.fromTypesafeConfig(rawConfig))
    config <- ZIO.fromEither(read(descriptor.from(configSource)))
  } yield config).toLayer.orDie
}
