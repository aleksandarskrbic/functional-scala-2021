package aggregation.service

import zio._
import zio.magic._
import aggregation.service.config.AppConfig
import aggregation.service.http.HttpServer
import aggregation.service.http.routes.QueryRoutes
import aggregation.service.kafka.AggregationProcessor
import aggregation.service.store.{TotalAmountByCountryStore, TotalAmountByUserStore}

object AggregationServiceApp extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    (for {
      aggregationProcessor <- ZIO.service[AggregationProcessor]
      httpServer <- ZIO.service[HttpServer]
      processorFiber <- aggregationProcessor.start().fork
      serverFiber <- httpServer.start().fork
      _ <- processorFiber.join
      _ <- serverFiber.join
    } yield ())
      .inject(
        AppConfig.live,
        TotalAmountByUserStore.live,
        TotalAmountByCountryStore.live,
        HttpServer.live,
        QueryRoutes.live,
        AggregationProcessor.live,
        ZEnv.live
      )
      .exitCode

}
