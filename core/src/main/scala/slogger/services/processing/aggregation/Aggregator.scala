package slogger.services.processing.aggregation

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import slogger.model.processing.Slice

/**
 * Aggregator class also should have one counstructor, that take one config: JsObject parameter 
 */
trait Aggregator {
  def name = this.getClass().getName()
  
  def aggregate(enumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[Map[String, BigDecimal]]
  
  /**
   * Merge all slices data to one total for whole period. 
   * Return None if it is impossible
   */
  def mergeSlices(slices: Seq[(Slice, Map[String, BigDecimal])]): Option[Map[String, BigDecimal]] = None 
}