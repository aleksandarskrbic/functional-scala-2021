package aggregation.service.http

import aggregation.service.config.AppConfig
import aggregation.service.http.routes.QueryRoutes
import zhttp.service.Server
import zio.ZIO
import zio.blocking.Blocking

class HttpServer(httpConfig: AppConfig.Http, queryRoutes: QueryRoutes) {
  def start(): ZIO[Blocking, Throwable, Nothing] =
    Server.start(
      httpConfig.port,
      queryRoutes.routes
    )
}

object HttpServer {
  val live = (for {
    appConfig <- ZIO.service[AppConfig]
    queryRoutes <- ZIO.service[QueryRoutes]
  } yield new HttpServer(appConfig.http, queryRoutes)).toLayer
}
