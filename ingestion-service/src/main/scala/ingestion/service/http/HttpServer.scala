package ingestion.service.http

import zhttp.service.Server
import zio.kafka.producer.Producer
import ingestion.service.config.AppConfig
import ingestion.service.http.routes.{HealthCheckRoutes, IngestionRoutes}
import zio.ZIO
import zio.blocking.Blocking

class HttpServer(appConfig: AppConfig) {
  private val httpConfig = appConfig.http
  private val producerConfig = appConfig.producer
  private val producerManaged = Producer.make(producerConfig.toProducerSettings)

  def start(): ZIO[Blocking, Throwable, Nothing] =
    producerManaged.use { producer =>
      val ingestionRoutes = new IngestionRoutes(producer, producerConfig).routes
      Server.start(
        httpConfig.port,
        HealthCheckRoutes.routes +++ ingestionRoutes
      )
    }
}
