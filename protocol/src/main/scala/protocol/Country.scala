package protocol

import zio.json._

object Country {
  final case class Country(
      name: String,
      capital: String,
      region: String,
      subregion: String,
      population: Long
  )

  implicit val codec: JsonCodec[Country] = DeriveJsonCodec.gen[Country]
}
