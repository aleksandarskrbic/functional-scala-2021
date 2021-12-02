package aggregation.service.config

import com.typesafe.config.ConfigFactory
import zio._
import zio.config.magnolia.DeriveConfigDescriptor
import zio.config.read
import zio.config.typesafe.TypesafeConfigSource
import zio.duration.Duration
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings

import scala.concurrent.duration.DurationInt

final case class AppConfig(
    http: AppConfig.Http,
    consumer: AppConfig.Consumer
)

object AppConfig {
  private val descriptor = DeriveConfigDescriptor.descriptor[AppConfig]

  final case class Http(port: Int)

  final case class Consumer(
      bootstrapServers: String,
      topic: String,
      groupId: String
  ) {
    def toConsumerSettings: ConsumerSettings =
      ConsumerSettings(bootstrapServers.split(",").toList)
        .withGroupId(groupId)
        .withClientId("client")
        .withCloseTimeout(Duration.fromScala(100.seconds))
        .withPollTimeout(Duration.fromMillis(10))
        .withProperty("enable.auto.commit", "false")
        .withProperty("auto.offset.reset", "earliest")
  }

  lazy val live: ZLayer[Any, Nothing, Has[AppConfig]] =
    (for {
      rawConfig <- ZIO.effect(
        ConfigFactory.load().getConfig("aggregation-service")
      )
      configSource <- ZIO.fromEither(
        TypesafeConfigSource.fromTypesafeConfig(rawConfig)
      )
      config <- ZIO.fromEither(read(descriptor.from(configSource)))
    } yield config).toLayer.orDie
}
