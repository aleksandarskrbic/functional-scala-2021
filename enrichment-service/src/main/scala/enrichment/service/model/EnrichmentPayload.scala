package enrichment.service.model

import zio.json.DeriveJsonCodec
import protocol.Country.Country

object EnrichmentPayload {
  final case class EnrichmentPayload(
      capital: List[String],
      region: String,
      subregion: String,
      population: Long
  ) {
    def toCountry(name: String): Country =
      Country(name, capital.head, region, subregion, population)
  }

  implicit val codec = DeriveJsonCodec.gen[EnrichmentPayload]
}
