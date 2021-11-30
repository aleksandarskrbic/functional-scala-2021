package enrichment.service

import zio._
import enrichment.service.config.AppConfig
import enrichment.service.http.EnrichmentService
import enrichment.service.kafka.EnrichmentProcessor
import sttp.client3.httpclient.zio.HttpClientZioBackend

object EnrichmentServiceApp extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    AppConfig
      .load()
      .flatMap { appConfig =>
        HttpClientZioBackend.managed().use { httpClient =>
          new EnrichmentProcessor(
            appConfig.consumer,
            appConfig.producer,
            new EnrichmentService(appConfig.enrichment, httpClient)
          ).start
        }
      }
      .exitCode
}
