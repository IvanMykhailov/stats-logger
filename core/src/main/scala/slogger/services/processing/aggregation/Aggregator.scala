package slogger.services.processing.aggregation

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * Aggregator class also should have one counstructor, that take one config: JsObject parameter 
 */
trait Aggregator {
  def name = this.getClass().getName()
  def aggregate(enumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[Map[String, BigDecimal]]
}