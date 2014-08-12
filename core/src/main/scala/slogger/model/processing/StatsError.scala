package slogger.model.processing

import play.api.libs.json.JsValue


case class StatsError(
  message: String,
  data: Option[JsValue]
)