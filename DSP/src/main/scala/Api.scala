import cats.data.OptionT
import cats.instances.list._
import cats.syntax.list._
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util._
import com.typesafe.config.ConfigFactory
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import io.circe.parser._
import request.DspAdReqBody
import response.DspAdResBody

import scala.language.postfixOps

object Api extends App {
  val config = ConfigFactory.load

  private[this] def createDspAdResBody(
    dspAdReqBody: DspAdReqBody
  ): DspAdResBody = {
    val price: Double = dspAdReqBody.floorPrice * 2
    println(price)

    DspAdResBody(
      url="http://example.com",
      price=price
    )
  }

  val response: Endpoint[DspAdResBody] = post("v1" :: "ad" :: jsonBody[DspAdReqBody]) { 
    (dspAdReqBody: DspAdReqBody) =>
      Ok(createDspAdResBody(dspAdReqBody))
  }

  val dspPort = config.getString("app.dspPort")
  val server: ListeningServer =
    Http.server.serve(s":$dspPort", response.toServiceAs[Application.Json])

  Await.ready(server)
}
