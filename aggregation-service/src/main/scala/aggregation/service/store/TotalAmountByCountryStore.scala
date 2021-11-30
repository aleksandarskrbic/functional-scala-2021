package aggregation.service.store

import zio.json._
import protocol.TransactionEnriched.TransactionEnriched

object TotalAmountByCountryStore {
  final case class TotalAmountByCountry(
      country: String,
      totalAmount: BigDecimal
  )

  implicit val encoder = DeriveJsonEncoder.gen[TotalAmountByCountry]

  val live =
    Store
      .make[String, TransactionEnriched, TotalAmountByCountry](
        initFn = transaction =>
          TotalAmountByCountry(
            transaction.country.name,
            transaction.amount
          ),
        aggregateFn = (transaction, totalAmountByCountry) =>
          totalAmountByCountry.copy(totalAmount = totalAmountByCountry.totalAmount + transaction.amount)
      )
      .toLayer
}
