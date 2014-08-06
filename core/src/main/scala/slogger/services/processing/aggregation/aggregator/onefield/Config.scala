package slogger.services.processing.aggregation.aggregator.onefield

import play.api.libs.functional.syntax.toInvariantFunctorOps
import play.api.libs.json.Json

case class Config(
  fieldName: String    
)

object Config {
  implicit val ConfigFormat = Json.format[Config]
}