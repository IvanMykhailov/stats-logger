package slogger.model.processing.aggregation.aggregators.onefield

import play.api.libs.json.Json

case class Config(
  fieldName: String    
)

object Config {
  implicit val ConfigFormat = Json.format[Config]
}