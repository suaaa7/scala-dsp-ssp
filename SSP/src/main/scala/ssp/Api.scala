package ssp

import cats.data.OptionT
import cats.instances.list._
import cats.syntax.list._
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import ssp.dsp.Dsp
import ssp.request.{DspAdReqBody, SspAdReqBody}
import ssp.response.{DspAdResBody, SspAdResBody}
import scala.collection.JavaConversions._

object Api extends App {
  val config = ConfigFactory.load

  private[this] def createDspAdReqBody(
    sspAdReqBody: SspAdReqBody
  ): DspAdReqBody = {
    val floorPrice: Double = 100.0

    DspAdReqBody(
      sspName=config.getString("app.sspName"),
      siteId=sspAdReqBody.siteId,
      adspotId=sspAdReqBody.adspotId,
      floorPrice=floorPrice 
    )
  }

  private[this] def request2Dsp(
    dsp: Dsp,
    dspAdReqBody: DspAdReqBody
  ): Future[Either[Throwable, Response]] = {
    val requestTimeout = config.getInt("app.requestTimeout")
    val client: Service[Request, Response] = Http.client
      .withRequestTimeout(requestTimeout.milliseconds)
      .newService(s"${dsp.host}:${dsp.port}")

    val request = Request(Method.Post, dsp.path).host(dsp.host)
    request.setContentString(dspAdReqBody.asJson.noSpaces)
    request.setContentTypeJson()

    client(request)
      .map(Right(_))
      .handle { case t => Left(t) }
  }

  val dspClients: List[Dsp] = {
    val dspHost = config.getString("app.dspHost")
    val dspPort = config.getString("app.dspPort")
    config.getStringList("app.dspPaths").toList.map { dspPath =>
      Dsp(dspHost, dspPort, dspPath)
    }
  }

  val response: Endpoint[SspAdResBody] = post("v1" :: "ad" :: jsonBody[SspAdReqBody]) { 
    (sspAdReqBody: SspAdReqBody) =>
      val dspAdReqBody = createDspAdReqBody(sspAdReqBody)

      Future
        .collect(dspClients.map(request2Dsp(_, dspAdReqBody)))
        .map { listOfEither =>
          (for {
            responseNel <- OptionT.fromOption[List](listOfEither.flatMap(_.toOption).toList.toNel)
            response <- OptionT.liftF(responseNel.toList)
            dspAdResBody <- OptionT.fromOption(decode[DspAdResBody](response.contentString).toOption)
          } yield dspAdResBody)
            .value.flatten.toNel.map { dspAdResNel =>
              Ok(
                SspAdResBody(dspAdResNel.toList.maxBy(_.price).url)
              )
            }.getOrElse(NoContent)
        }
  }

  val sspPort = config.getString("app.sspPort")
  val server = Http.server.serve(s":$sspPort", response.toServiceAs[Application.Json])

  Await.ready(server)
}
