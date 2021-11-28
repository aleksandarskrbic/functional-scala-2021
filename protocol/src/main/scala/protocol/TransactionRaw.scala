package protocol

import zio.json._

object TransactionRaw {
  final case class TransactionRaw(
      userId: Long,
      country: String,
      amount: BigDecimal
  )

  implicit val codec: JsonCodec[TransactionRaw] =
    DeriveJsonCodec.gen[TransactionRaw]
}
