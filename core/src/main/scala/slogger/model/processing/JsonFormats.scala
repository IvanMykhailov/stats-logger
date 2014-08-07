package slogger.model.processing

import play.api.libs.json._


trait JsonFormats {
  
  implicit val SliceFormat: Format[Slice] = Json.format[Slice]
  
  implicit val SliceAggregatedFormat = Json.format[SliceAggregated]
}


object JsonFormats extends JsonFormats