package slogger.services.processing.aggregation.aggregator

import play.api.libs.json._


object AggregatorUtils {
  def values(js: JsValue): Seq[String] = js match {
    case JsArray(values) => values.map(_.toString)
    case obj: JsObject => Seq()
    case v: JsValue => Seq(v.toString)
  }
}