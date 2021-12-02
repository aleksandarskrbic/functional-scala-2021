package enrichment.service.http

import zio._
import sttp.model.Uri
import sttp.client3.ziojson.asJson
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import enrichment.service.config.AppConfig
import enrichment.service.model.EnrichmentPayload.EnrichmentPayload

class EnrichmentService(
    enrichmentConfig: AppConfig.Enrichment,
    httpClient: SttpBackend[Task, ZioStreams with WebSockets]
) {

  def fetchCountryDetails(countryName: String): IO[String, EnrichmentPayload] =
    (for {
      response <- httpClient.send(
        basicRequest
          .get(urlOf(countryName))
          .response(asJson[List[EnrichmentPayload]])
      )
      result <- response.body.fold(
        error => ZIO.fail(error.toString),
        parsed => ZIO.succeed(parsed.head)
      )
    } yield result).orElseFail("Unable to fetch country details")

  private def urlOf(countryName: String): Uri =
    uri"${enrichmentConfig.url}$countryName"
}

object EnrichmentService {
  lazy val live = (for {
    appConfig <- ZIO.service[AppConfig]
    httpClient <- ZIO.service[SttpBackend[Task, ZioStreams with WebSockets]]
  } yield new EnrichmentService(appConfig.enrichment, httpClient)).toLayer
}
