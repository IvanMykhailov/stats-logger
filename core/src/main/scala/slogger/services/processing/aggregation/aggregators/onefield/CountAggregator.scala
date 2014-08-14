package slogger.services.processing.aggregation.aggregators.onefield

import slogger.services.processing.aggregation.Aggregator
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.concurrent.Future
import slogger.services.processing.aggregation.aggregators.AggregatorUtils
import slogger.model.processing.Slice
import slogger.model.processing.SliceResult
import slogger.utils.IterateeUtils
import play.api.libs.iteratee.Step
import slogger.model.processing.AggregationException
import play.api.libs.iteratee.Input
import slogger.services.processing.aggregation.aggregators.FoldAggregator


/**
 * Return count of each value for all found values in specified field.
 * Field can be array of simple types. In that case each array element is count as separate value 
 */
class CountAggregator(config: JsObject) extends FoldAggregator[Map[String, BigDecimal]] {
  val cfg = config.as[Config]
  
  override def name = "SimpleCountAggregator"

    
  //Slice aggregation
  protected def foldInitState = Map.empty
  
  protected def folder(state: Map[String, BigDecimal], json: JsObject) = 
    AggregatorUtils.stringValues(json\(cfg.fieldName)).foldLeft(state) { (rez, v) => 
      val count = rez.getOrElse(v, BigDecimal(0)) + 1
      rez + (v -> count)
    }
  
  protected def resultMapper(slice: Slice, rez: Map[String, BigDecimal]) = 
    SliceResult(
      slice,
      results = rez
    )
    
  
  //Total aggregation
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceResult]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(_ + _) _
    merger(slices.map(_.results))
  } 
  
}