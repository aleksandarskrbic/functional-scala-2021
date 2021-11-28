package enrichment.service

import org.slf4j.LoggerFactory
import io.github.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}

object EmbeddedKafkaRunner extends App with EmbeddedKafka {
  val log = LoggerFactory.getLogger(this.getClass)

  val port = 9092

  implicit val config =
    EmbeddedKafkaConfig(kafkaPort = port, zooKeeperPort = 5555)

  val embeddedKafkaServer = EmbeddedKafka.start()

  createCustomTopic(topic = "transactions.raw", partitions = 3)
  createCustomTopic(topic = "transactions.enriched", partitions = 3)
  log.info(s"Kafka running: localhost:$port")

  embeddedKafkaServer.broker.awaitShutdown()
}
