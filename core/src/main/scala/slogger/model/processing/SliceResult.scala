package slogger.model.processing

import play.api.libs.json._

case class SliceResult(
  slice: Slice,
  results: Map[String, BigDecimal],
  meta: JsObject = Json.obj() //Some aggregation (like average) require additional info to merge slices
                              //can't use JsObject due to bug in play-reactivemongo 
)