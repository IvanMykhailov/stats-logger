package slogger.model.specification.aggregation

import play.api.libs.json.JsObject
import slogger.services.processing.aggregation.Aggregator


case class AggregationSpecs(
  aggregatorClass: String,
  config: JsObject
)