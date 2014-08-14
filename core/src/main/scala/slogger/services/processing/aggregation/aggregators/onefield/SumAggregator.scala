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
import play.api.libs.iteratee.Enumeratee
import slogger.services.processing.aggregation.aggregators.FoldAggregator


class SumAggregator(config: JsObject) extends FoldAggregator[BigDecimal] {
  val cfg = config.as[Config]
  
  val resultKey = "[SUM]"
  
  override def name = "SimpleSumAggregator"
    
  //Slice aggregation
  protected def foldInitState = BigDecimal(0)
  
  protected def folder(state: BigDecimal, json: JsObject) = AggregatorUtils.numberValues(json\(cfg.fieldName)).fold(state)(_ + _)
  
  protected def resultMapper(slice: Slice, sum: BigDecimal) = 
    SliceResult(
      slice,
      results = Map(resultKey -> sum)
    )
    
    
  //Total aggregation
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceResult]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(_ + _) _
    merger(slices.map(_.results))
  } 
  
}