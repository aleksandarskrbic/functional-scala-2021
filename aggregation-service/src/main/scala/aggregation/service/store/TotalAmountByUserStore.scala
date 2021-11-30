package aggregation.service.store

import zio.json._
import protocol.TransactionEnriched.TransactionEnriched

object TotalAmountByUserStore {
  final case class TotalAmountByUser(userId: Long, totalAmount: BigDecimal)

  implicit val encoder = DeriveJsonEncoder.gen[TotalAmountByUser]

  val live =
    Store
      .make[Long, TransactionEnriched, TotalAmountByUser](
        initFn = transaction =>
          TotalAmountByUser(
            transaction.userId,
            transaction.amount
          ),
        aggregateFn = (transaction, totalAmountByUser) =>
          totalAmountByUser.copy(totalAmount = totalAmountByUser.totalAmount + transaction.amount)
      )
      .toLayer
}
