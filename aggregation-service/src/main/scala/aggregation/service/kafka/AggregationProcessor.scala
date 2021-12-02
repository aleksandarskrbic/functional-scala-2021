package aggregation.service.kafka

import zio._
import zio.json._
import zio.kafka.serde.Serde
import zio.kafka.consumer.{Consumer, Subscription}
import protocol.TransactionEnriched._
import aggregation.service.config.AppConfig
import aggregation.service.store.Store
import aggregation.service.store.TotalAmountByCountryStore.TotalAmountByCountry
import aggregation.service.store.TotalAmountByUserStore.TotalAmountByUser

class AggregationProcessor(
    consumerConfig: AppConfig.Consumer,
    consumer: Consumer,
    totalAmountByCountryStore: Store[String, TransactionEnriched, TotalAmountByCountry],
    totalAmountByUserStore: Store[Long, TransactionEnriched, TotalAmountByUser]
) {
  def start() =
    consumer
      .subscribeAnd(Subscription.topics(consumerConfig.topic))
      .plainStream(Serde.long, Serde.string)
      .mapMPar(4) { committableRecord =>
        val parsed = committableRecord.value.fromJson[TransactionEnriched]

        parsed match {
          case Right(transaction) =>
            totalAmountByUserStore
              .append(transaction.userId, transaction)
              .zipPar(totalAmountByCountryStore.append(transaction.country.name, transaction))
              .as(committableRecord.offset)
          case Left(_) => ZIO.succeed(committableRecord.offset)
        }
      }
      .aggregateAsync(Consumer.offsetBatches)
      .mapM(_.commit)
      .runDrain
}

object AggregationProcessor {
  val live =
    (for {
      appConfig <- ZIO.service[AppConfig]
      consumer <- ZIO.service[Consumer]
      totalAmountByCountryStore <- ZIO.service[Store[String, TransactionEnriched, TotalAmountByCountry]]
      totalAmountByUserStore <- ZIO.service[Store[Long, TransactionEnriched, TotalAmountByUser]]
    } yield new AggregationProcessor(
      appConfig.consumer,
      consumer,
      totalAmountByCountryStore,
      totalAmountByUserStore
    )).toLayer
}
