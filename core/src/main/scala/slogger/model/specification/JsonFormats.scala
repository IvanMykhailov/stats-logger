package slogger.model.specification

import play.api.libs.json.Json


trait JsonFormats 
  extends slogger.model.specification.aggregation.JsonFormats
  with slogger.model.specification.extraction.JsonFormats
{
  implicit val CalculationSpecsFormat = Json.format[CalculationSpecs]
}


object JsonFormats extends JsonFormats