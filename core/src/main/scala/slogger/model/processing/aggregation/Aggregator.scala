package slogger.model.processing.aggregation

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future


trait Aggregator {
  def name: String
  def aggregate(enumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[Map[String, BigDecimal]]
}