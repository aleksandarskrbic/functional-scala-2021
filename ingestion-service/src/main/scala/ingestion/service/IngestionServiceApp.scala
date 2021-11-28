package ingestion.service

import zio._
import ingestion.service.config.AppConfig
import ingestion.service.http.HttpServer

object IngestionServiceApp extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    AppConfig
      .load()
      .flatMap { appConfig =>
        new HttpServer(appConfig).start()
      }
      .exitCode
}
