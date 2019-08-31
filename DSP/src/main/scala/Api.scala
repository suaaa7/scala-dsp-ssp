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
import cache.{CCache, GCache}
import request.DspAdReqBody
import response.DspAdResBody

import scala.language.postfixOps
import scala.util.Random

object Api extends App {
  val config = ConfigFactory.load

  private[this] def createDspAdResBodyG(
    dspAdReqBody: DspAdReqBody
  ): Future[DspAdResBody] = {
    for {
      mapFromGCache <- GCache.getMapCache("Guava")
    } yield {
      val price = dspAdReqBody.floorPrice * mapFromGCache.getOrElse("A", 0.001)
      DspAdResBody(
        url="http://example.com",
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
      val price = dspAdReqBody.floorPrice * mapFromCCache.getOrElse("A", 0.001)
      DspAdResBody(
        url="http://example.com",
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
  val server: ListeningServer =
    Http.server.serve(s":$dspPort", service)

  Await.ready(server)
}
