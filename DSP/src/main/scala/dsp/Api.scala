package dsp

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import com.typesafe.config.ConfigFactory
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import dsp.cache.{CCache, GCache}
import dsp.request.DspAdReqBody
import dsp.response.DspAdResBody

object Api extends App {
  val config = ConfigFactory.load

  private[this] def createDspAdResBodyG(
    dspAdReqBody: DspAdReqBody
  ): Future[DspAdResBody] = {
    for {
      mapFromGCache <- GCache.getMapCache("Guava")
    } yield {
      val price = dspAdReqBody.floorPrice * mapFromGCache.getOrElse("A", 1.0)
      DspAdResBody(
        url="http://example.com/g",
        price=price
      )
    }
  }

  private[this] def createDspAdResBodyC(
    dspAdReqBody: DspAdReqBody
  ): Future[DspAdResBody] = {
    for {
      mapFromCCache <- CCache.getMapCache("Caffeine")
    } yield {
      val price = dspAdReqBody.floorPrice * mapFromCCache.getOrElse("A", 1.0)
      DspAdResBody(
        url="http://example.com/c",
        price=price
      )
    }
  }

  val responseG: Endpoint[DspAdResBody] = post("v1" :: "ad" :: "g" :: jsonBody[DspAdReqBody]) { 
    (dspAdReqBody: DspAdReqBody) =>
      for {
        dspAdResBody <- createDspAdResBodyG(dspAdReqBody)
      } yield Ok(dspAdResBody)
  }

  val responseC: Endpoint[DspAdResBody] = post("v1" :: "ad" :: "c" :: jsonBody[DspAdReqBody]) { 
    (dspAdReqBody: DspAdReqBody) =>
      for {
        dspAdResBody <- createDspAdResBodyC(dspAdReqBody)
      } yield Ok(dspAdResBody)
  }

  val service = Bootstrap
    .serve[Application.Json](responseG)
    .serve[Application.Json](responseC)
    .toService

  val dspPort = config.getString("app.dspPort")
  val server = Http.server.serve(s":$dspPort", service)

  Await.ready(server)
}
