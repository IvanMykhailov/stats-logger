package slogger.services.processing.aggregation.aggregators.twofield

import play.api.libs.json.Json
import play.api.libs.json.JsValue
import slogger.services.processing.extraction.ExtractionUtils


case class Config(
  keyFieldName: String,
  valueFieldName: String
) {
  
  def extractKeyField(json: JsValue): JsValue = ExtractionUtils.extractByPath(json, keyFieldName)
  
  def extractValueField(json: JsValue): JsValue = ExtractionUtils.extractByPath(json, valueFieldName)
}

object Config {
  implicit val ConfigFormat = Json.format[Config]
}