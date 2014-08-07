package slogger.model.processing

import play.api.libs.json.JsObject

case class SliceAggregated(
  slice: Slice,
  results: Map[String, BigDecimal],
  meta: Map[String, JsObject]//Some aggregation (like average) require additional info to merge slices
)