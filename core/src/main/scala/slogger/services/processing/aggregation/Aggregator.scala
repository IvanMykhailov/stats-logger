package slogger.services.processing.aggregation

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import slogger.model.processing.Slice
import slogger.model.processing.SliceAggregated
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Aggregator class also should have one counstructor, that take one config: JsObject parameter 
 */
trait Aggregator {
  def name = this.getClass().getName()
  
  def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceAggregated]
  
  /**
   * if true than mergeSlices should be implemented
   */
  def isSliceMergingSupported: Boolean = false
  
  /**
   * Merge all slices data to one total for whole period. 
   * Throw NotImplementedException if it is impossible, in that case isSliceMergingSupported should be false
   */
  def mergeSlices(slices: Seq[SliceAggregated]): Map[String, BigDecimal] = throw new NotImplementedException 
}