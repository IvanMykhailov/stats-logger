package slogger.services.processing.aggregation.aggregators.onefield

import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import slogger.services.processing.extraction.ExtractionUtils

case class Config(
  fieldName: String    
) {
  
  def extractField(json: JsValue): JsValue = ExtractionUtils.extractByPath(json, fieldName)
}

object Config {
  implicit val ConfigFormat = Json.format[Config]
}