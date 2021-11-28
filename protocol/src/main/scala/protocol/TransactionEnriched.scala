package protocol

import zio.json._
import protocol.Country._

object TransactionEnriched {
  final case class TransactionEnriched(userId: Long, country: Country, amount: BigDecimal)

  implicit val codec: JsonCodec[TransactionEnriched] = DeriveJsonCodec.gen[TransactionEnriched]
}
