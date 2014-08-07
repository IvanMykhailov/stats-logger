package slogger.model.processing

import play.api.libs.json._

case class SliceAggregated(
  slice: Slice,
  results: Map[String, BigDecimal],
  meta: JsValue = JsNull //Some aggregation (like average) require additional info to merge slices
)