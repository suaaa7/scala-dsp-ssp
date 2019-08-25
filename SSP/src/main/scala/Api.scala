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
import request.{SspAdReqBody, DspAdReqBody}
import response.{SspAdResBody, DspAdResBody}

import scala.language.postfixOps

object Api extends App {
  val config = ConfigFactory.load

  private[this] def createDspAdReqBody(
    sspAdReqBody: SspAdReqBody
  ): DspAdReqBody = {
    val floorPrice: Double = 1.08

    DspAdReqBody(
      sspName=config.getString("app.sspName"),
      siteId=sspAdReqBody.siteId,
      adspotId=sspAdReqBody.adspotId,
      floorPrice=floorPrice 
    )
  }

  private[this] def request2Dsp(
    host: String,
    port: String,
    json: String
  ): Future[Either[Throwable, Response]] = {
    val requestTimeout = config.getInt("app.requestTimeout")
    val client: Service[Request, Response] = Http.client
      .withRequestTimeout(requestTimeout.milliseconds)
      .newService(s"""$host:$port""")

    val request = Request(Method.Get, "/").host(host)
    request.setContentString(json)
    request.setContentTypeJson()

    client(request)
      .map(Right(_))
      .handle { case t => Left(t) }
  }

  val response: Endpoint[SspAdResBody] = post("v1" :: "ad" :: jsonBody[SspAdReqBody]) { 
    (sspAdReqBody: SspAdReqBody) =>
      val dspHost = config.getString("app.dspHost")
      val dspPort = config.getString("app.dspPort")
      val listOfFutures = 
        List(request2Dsp(dspHost, dspPort, createDspAdReqBody(sspAdReqBody).toString))
      val futureOfList = Future.collect(listOfFutures)

      futureOfList.map { listOfEither =>
        val responsesReceivedInTime = listOfEither.flatMap(_.toOption)

        val dspAdResList: List[DspAdResBody] = (for {
          responseNel <- OptionT.fromOption[List](responsesReceivedInTime.toList.toNel)
          response <- OptionT.liftF(responseNel.toList)
          dspAdResBody <- OptionT.fromOption(decode[DspAdResBody](response.contentString).toOption)
        } yield dspAdResBody).value.flatten

        dspAdResList.toNel.map { dspAdResNel =>
          Ok(
            SspAdResBody(dspAdResNel.toList.maxBy(_.price).url)
          )
        }.getOrElse(NoContent)
      }
  }

  val sspPort = config.getString("app.sspPort")
  val server: ListeningServer =
    Http.server.serve(s":$sspPort", response.toServiceAs[Application.Json])

  Await.ready(server)
}
