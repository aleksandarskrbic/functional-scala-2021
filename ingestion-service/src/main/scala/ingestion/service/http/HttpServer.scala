package ingestion.service.http

import zio._
import zio.blocking.Blocking
import zhttp.service.Server
import ingestion.service.config.AppConfig
import ingestion.service.http.routes.{HealthCheck, Ingestion}

class HttpServer(
    httpConfig: AppConfig.Http,
    ingestionRoutes: Ingestion
) {
  def start(): ZIO[Blocking, Throwable, Nothing] =
    Server.start(
      httpConfig.port,
      HealthCheck.routes +++ ingestionRoutes.routes
    )
}

object HttpServer {
  lazy val live = (for {
    appConfig <- ZIO.service[AppConfig]
    ingestionRoutes <- ZIO.service[Ingestion]
  } yield new HttpServer(appConfig.http, ingestionRoutes)).toLayer
}
