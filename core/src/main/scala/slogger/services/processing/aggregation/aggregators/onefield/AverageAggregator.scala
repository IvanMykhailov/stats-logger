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
import play.api.libs.json.Json
import play.api.libs.json.Format
import slogger.services.processing.aggregation.aggregators.FoldAggregator


class AverageAggregator(config: JsObject) extends FoldAggregator[AverageAggregator.TmpRez] {
  import AverageAggregator._
  
  val cfg = config.as[Config]
  
  val resultKey = "[AVERAGE]"
  
  override def name = "SimpleSumAggregator"
   
    
  //Slice aggregation
  protected def foldInitState = TmpRez()
  
  protected def folder(state: TmpRez, json: JsObject) = {
    val values = AggregatorUtils.numberValues(cfg.extractField(json))
      if (values.isEmpty) {
        state
      } else {
        TmpRez(
          count = state.count + values.length,
          sum = state.sum + values.reduce(_ + _)
        )
      }    
  }
  
  protected def resultMapper(slice: Slice, tmpRez: TmpRez) = 
    SliceResult(
      slice,
      results = Map(resultKey -> safeDiv(tmpRez.sum, tmpRez.count)),
      meta = Json.toJson(tmpRez).as[JsObject]
    )
    
  def safeDiv(bd: BigDecimal, div: BigDecimal): BigDecimal = if (bd == 0) 0 else {bd / div}
  
  
  //Total aggregation
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceResult]): Map[String, BigDecimal] = {
    val tmpRez = slices.map(_.meta.as[TmpRez]).reduce(_ + _)
    Map(resultKey -> safeDiv(tmpRez.sum, tmpRez.count))
  } 
}


object AverageAggregator {
  case class TmpRez(count: BigDecimal = 0, sum: BigDecimal = 0) {
    def + (other: TmpRez) = this.copy(
      count = this.count + other.count,
      sum = this.sum + other.sum
    )
  }
  
  implicit val TmpRezFormat: Format[TmpRez] = Json.format[TmpRez]  
}
