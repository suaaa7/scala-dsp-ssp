import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Await
import com.typesafe.config.ConfigFactory
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import request.AdReqBody

object Api extends App {
  val config = ConfigFactory.load

  val response: Endpoint[AdReqBody] = post("v1" :: "ad" :: jsonBody[AdReqBody]) {
    (body: AdReqBody) =>
      Ok(body)
  }

  val sspPort = config.getString("app.sspPort")
  val server: ListeningServer =
    Http.server.serve(s":$sspPort", response.toServiceAs[Application.Json])

  Await.ready(server)
}
