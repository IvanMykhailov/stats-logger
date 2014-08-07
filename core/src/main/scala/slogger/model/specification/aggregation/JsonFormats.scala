package slogger.model.specification.aggregation

import play.api.libs.json.Json


trait JsonFormats {
  implicit val AggregationSpecsFormat = Json.format[AggregationSpecs] 
}


object JsonFormats extends JsonFormats