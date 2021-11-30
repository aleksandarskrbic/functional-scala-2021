package enrichment.service.kafka

import zio._
import zio.json._
import protocol.Country._
import protocol.TransactionRaw._
import protocol.TransactionEnriched._
import zio.kafka.producer.Producer
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.serde.Serde
import enrichment.service.config.AppConfig
import enrichment.service.http.EnrichmentService
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._

class EnrichmentProcessor(
    consumerConfig: AppConfig.Consumer,
    producerConfig: AppConfig.Producer,
    enrichmentService: EnrichmentService
) {

  def start: ZIO[Clock with Blocking, Throwable, Unit] =
    (for {
      consumer <- Consumer.make(consumerConfig.toConsumerSettings)
      producer <- Producer.make(producerConfig.toProducerSettings)
    } yield consumer -> producer).use { case (consumer, producer) =>
      consumer
        .subscribeAnd(Subscription.topics(consumerConfig.topic))
        .plainStream(Serde.long, Serde.string)
        .mapMPar(4) { committableRecord =>
          val parsed = committableRecord.value.fromJson[TransactionRaw]

          parsed match {
            case Right(transactionRaw) =>
              for {
                enrichmentPayload <- enrichmentService
                  .fetchCountryDetails(
                    transactionRaw.country
                  )
                  .mapError(msg => new RuntimeException(msg))
                  .retry(Schedule.exponential(50.millis) && Schedule.recurs(4))
                country = enrichmentPayload.toCountry(transactionRaw.country)
                transactionEnriched = toTransactionEnriched(
                  transactionRaw,
                  country
                )
                _ <- producer.produceAsync(
                  producerConfig.topic,
                  transactionEnriched.userId,
                  transactionEnriched.toJsonPretty,
                  Serde.long,
                  Serde.string
                )
              } yield committableRecord.offset
            case Left(_) => ZIO.succeed(committableRecord.offset)
          }
        }
        .aggregateAsync(Consumer.offsetBatches)
        .mapM(_.commit)
        .runDrain
    }

  private def toTransactionEnriched(
      transactionRaw: TransactionRaw,
      country: Country
  ) =
    TransactionEnriched(transactionRaw.userId, country, transactionRaw.amount)
}
