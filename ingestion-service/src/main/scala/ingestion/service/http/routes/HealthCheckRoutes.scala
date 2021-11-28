package ingestion.service.http.routes

import zhttp.http._

object HealthCheckRoutes {
  val routes = Http.collect[Request] {
    case Method.GET -> Root / "health-check" / "app" => Response.ok
  }
}
