package slogger.model.specification

import play.api.libs.json.Json


trait JsonFormats 
  extends slogger.model.specification.aggregation.JsonFormats
  with slogger.model.specification.extraction.JsonFormats
{
  implicit val BundleFormat = Json.format[Bundle]
}


object JsonFormats extends JsonFormats