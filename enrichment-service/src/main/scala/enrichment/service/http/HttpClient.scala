package enrichment.service.http

import sttp.client3.httpclient.zio.HttpClientZioBackend

object HttpClient {
  lazy val live = HttpClientZioBackend.managed().toLayer
}
