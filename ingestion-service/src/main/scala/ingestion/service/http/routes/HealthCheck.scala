package ingestion.service.http.routes

import zhttp.http._

object HealthCheck {
  val routes = Http.collect[Request] { case Method.GET -> Root / "health-check" / "app" =>
    Response.ok
  }
}
