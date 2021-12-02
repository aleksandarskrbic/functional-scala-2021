package enrichment.service

import zio._
import zio.magic._
import enrichment.service.config.AppConfig
import enrichment.service.http.{EnrichmentService, HttpClient}
import enrichment.service.kafka.{EnrichmentProcessor, KafkaSettings}
import zio.clock.Clock
import zio.kafka.consumer.Consumer
import zio.kafka.producer.Producer

object EnrichmentServiceApp extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      enrichmentProcessor <- ZIO.service[EnrichmentProcessor]
      _ <- enrichmentProcessor.start
    } yield ())
      .inject(
        AppConfig.live,
        KafkaSettings.consumerSettingsLive,
        KafkaSettings.producerSettingsLive,
        Consumer.live,
        Producer.live,
        EnrichmentProcessor.live,
        EnrichmentService.live,
        HttpClient.live,
        ZEnv.live
      )
      .exitCode
}
