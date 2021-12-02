package aggregation.service

import zio._
import zio.magic._
import aggregation.service.config.AppConfig
import aggregation.service.http.HttpServer
import aggregation.service.http.routes.QueryRoutes
import aggregation.service.kafka.{AggregationProcessor, KafkaSettings}
import aggregation.service.store.{TotalAmountByCountryStore, TotalAmountByUserStore}
import zio.kafka.consumer.Consumer

object AggregationServiceApp extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      httpServer <- ZIO.service[HttpServer]
      aggregationProcessor <- ZIO.service[AggregationProcessor]
      serverFiber <- httpServer.start().fork
      processorFiber <- aggregationProcessor.start().fork
      _ <- ZIO.raceAll(serverFiber.await, List(processorFiber.await))
    } yield ())
      .inject(
        AppConfig.live,
        TotalAmountByUserStore.live,
        TotalAmountByCountryStore.live,
        HttpServer.live,
        QueryRoutes.live,
        AggregationProcessor.live,
        KafkaSettings.consumerSettingsLive,
        Consumer.live,
        ZEnv.live
      )
      .exitCode
}
