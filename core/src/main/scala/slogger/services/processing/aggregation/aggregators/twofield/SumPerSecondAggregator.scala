package slogger.services.processing.aggregation.aggregators.twofield

import play.api.libs.json._
import slogger.services.processing.aggregation.aggregators.FoldAggregator
import slogger.services.processing.aggregation.aggregators.AggregatorUtils
import slogger.model.processing.Slice
import slogger.model.processing.SliceResult


/**
 * TODO: Create tests
 * 
 */
class SumPerSecondAggregator(config: JsObject) extends FoldAggregator[Map[String, BigDecimal]] {
  val cfg = config.as[Config]
  
  override def name = "TwofieldSumAggregator"

    
  //Slice aggregation
  protected def foldInitState = Map.empty
  
  protected def folder(state: Map[String, BigDecimal], json: JsObject) = 
    AggregatorUtils.stringValues(cfg.extractKeyField(json)).foldLeft(state) { (rez, key) =>
      val recordSum = AggregatorUtils.numberValues(cfg.extractValueField(json)).sum      
      val totalSum = rez.getOrElse(key, BigDecimal(0)) + recordSum 
      rez + (key -> totalSum)
    }
  
  protected def resultMapper(slice: Slice, rez: Map[String, BigDecimal]) = { 
    val secondsInSlice = slice.toInterval.toDurationMillis() / 1000
    SliceResult(
      slice,
      results = rez.mapValues(_ / secondsInSlice)
    )
  }
  
  //Total aggregation
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceResult]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(_ + _) _
    merger(slices.map(_.results)).mapValues(_ / Math.max(slices.size, 1))
  } 
  
}