package ingestion.service.http.routes

import ingestion.service.config.AppConfig
import protocol.TransactionRaw._
import zhttp.http._
import zio._
import zio.json._
import zio.kafka.producer.Producer
import zio.kafka.serde._

final class IngestionRoutes(
    producer: Producer,
    producerConfig: AppConfig.Producer
) {
  val routes = Http.collectM[Request] {
    case req @ Method.POST -> Root / "ingestion" / "push-event" =>
      extractBodyOrFail(req) { transactionRaw =>
        producer
          .produceAsync(
            producerConfig.topic,
            transactionRaw.userId,
            transactionRaw.toJsonPretty,
            Serde.long,
            Serde.string
          )
          .as(Response.ok)
      }
  }

  private def extractBodyOrFail(
      req: Request
  )(fn: TransactionRaw => ZIO[Any, Throwable, UResponse]) =
    req.getBodyAsString match {
      case Some(bodyAsString) =>
        bodyAsString.fromJson[TransactionRaw] match {
          case Right(v) => fn(v)
          case Left(_) =>
            ZIO.succeed(Response.http(status = Status.BAD_REQUEST))
        }
      case None => ZIO.succeed(Response.http(status = Status.BAD_REQUEST))
    }
}
