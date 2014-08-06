package slogger.model.specification.aggregation

import play.api.libs.json.JsObject
import slogger.services.processing.aggregation.Aggregator


case class Aggregation(
  aggregatorClass: String,
  config: JsObject
)