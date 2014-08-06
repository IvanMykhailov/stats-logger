package slogger.model.specification.aggregation

import play.api.libs.json.Json


trait JsonFormats {
  implicit val AggregationFormat = Json.format[Aggregation] 
}


object JsonFormats extends JsonFormats