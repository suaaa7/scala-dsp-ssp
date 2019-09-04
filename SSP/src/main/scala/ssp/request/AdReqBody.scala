package ssp.request

case class SspAdReqBody(
  siteId: Int,
  adspotId: Int
)

case class DspAdReqBody(
  sspName: String,
  siteId: Int,
  adspotId: Int,
  floorPrice: Double
)
