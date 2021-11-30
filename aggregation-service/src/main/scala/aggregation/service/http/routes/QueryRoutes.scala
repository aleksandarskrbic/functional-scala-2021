package aggregation.service.http.routes

import zio._
import zio.json._
import zhttp.http._
import aggregation.service.store.Store
import aggregation.service.store.TotalAmountByCountryStore._
import aggregation.service.store.TotalAmountByUserStore._
import protocol.TransactionEnriched.TransactionEnriched

class QueryRoutes(
    totalAmountByCountryStore: Store[String, TransactionEnriched, TotalAmountByCountry],
    totalAmountByUserStore: Store[Long, TransactionEnriched, TotalAmountByUser]
) {
  val routes = Http.collectM[Request] {
    case Method.GET -> Root / "total-amount" / "country" / name =>
      totalAmountByCountryStore.find(name.capitalize).map {
        case Some(totalAmountByCountry) => Response.jsonString(totalAmountByCountry.toJsonPretty)
        case None                       => Response.jsonString("No data available")
      }
    case Method.GET -> Root / "total-amount" / "user" / id =>
      ZIO
        .effect(id.toLong)
        .flatMap { id =>
          totalAmountByUserStore.find(id).map {
            case Some(totalAmountByUser) => Response.jsonString(totalAmountByUser.toJsonPretty)
            case None                    => Response.jsonString("No data available")
          }
        }
        .catchAll(_ => ZIO.succeed(Response.jsonString("Invalid parameter")))
  }
}

object QueryRoutes {
  val live =
    (for {
      totalAmountByCountryStore <- ZIO.service[Store[String, TransactionEnriched, TotalAmountByCountry]]
      totalAmountByUserStore <- ZIO.service[Store[Long, TransactionEnriched, TotalAmountByUser]]
    } yield new QueryRoutes(totalAmountByCountryStore, totalAmountByUserStore)).toLayer
}
