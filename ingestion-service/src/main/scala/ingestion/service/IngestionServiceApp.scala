package ingestion.service

import zio._
import zio.magic._
import ingestion.service.http.HttpServer
import ingestion.service.config.AppConfig
import ingestion.service.kafka.EventProducerSettings
import ingestion.service.http.routes.Ingestion
import zio.kafka.producer.Producer

object IngestionServiceApp extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      httpServer <- ZIO.service[HttpServer]
      _ <- httpServer.start()
    } yield ())
      .inject(
        AppConfig.live,
        EventProducerSettings.live,
        Producer.live,
        Ingestion.live,
        HttpServer.live,
        ZEnv.live
      )
      .exitCode
}
