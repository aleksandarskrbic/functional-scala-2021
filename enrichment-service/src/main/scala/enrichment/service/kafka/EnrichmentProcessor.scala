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
    consumer: Consumer,
    producer: Producer,
    enrichmentService: EnrichmentService
) {
  def start: ZIO[Clock with Blocking, Throwable, Unit] =
    consumer
      .subscribeAnd(Subscription.topics(consumerConfig.topic))
      .plainStream(Serde.long, Serde.string)
      .mapMPar(8) { committableRecord =>
        ZIO.effectTotal {
          val parsed = committableRecord.value.fromJson[TransactionRaw]
          (committableRecord.offset, parsed)
        }
      }
      .mapMPar(8) { case (offset, parsed) =>
        parsed match {
          case Right(transactionRaw) =>
            for {
              transactionEnriched <- enrichTransaction(transactionRaw)
                .mapError(msg => new RuntimeException(msg))
                .retry(Schedule.exponential(50.millis) && Schedule.recurs(4))
              _ <- producer.produceAsync(
                producerConfig.topic,
                transactionEnriched.userId,
                transactionEnriched.toJsonPretty,
                Serde.long,
                Serde.string
              )
            } yield offset
          case Left(_) => ZIO.succeed(offset)
        }
      }
      .aggregateAsync(Consumer.offsetBatches)
      .mapM(_.commit)
      .runDrain

  private def enrichTransaction(transactionRaw: TransactionRaw) =
    enrichmentService
      .fetchCountryDetails(transactionRaw.country)
      .map { enrichmentPayload =>
        toTransactionEnriched(
          transactionRaw,
          enrichmentPayload.toCountry(transactionRaw.country)
        )
      }

  private def toTransactionEnriched(transactionRaw: TransactionRaw, country: Country) =
    TransactionEnriched(transactionRaw.userId, country, transactionRaw.amount)
}

object EnrichmentProcessor {
  lazy val live = (for {
    appConfig <- ZIO.service[AppConfig]
    consumer <- ZIO.service[Consumer]
    producer <- ZIO.service[Producer]
    enrichmentService <- ZIO.service[EnrichmentService]
  } yield new EnrichmentProcessor(
    appConfig.consumer,
    appConfig.producer,
    consumer,
    producer,
    enrichmentService
  )).toLayer
}
