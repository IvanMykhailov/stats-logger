package slogger.model.processing

import play.api.libs.json._


trait JsonFormats {
  
  val SliceFormat: Format[Slice] = Json.format[Slice]
}


object JsonFormats extends JsonFormats