package dsp.request

case class DspAdReqBody(
  sspName: String,
  siteId: Int,
  adspotId: Int,
  floorPrice: Double
)
