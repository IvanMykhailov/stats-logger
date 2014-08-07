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
import slogger.model.processing.SliceAggregated
import slogger.utils.IterateeUtils


class SumAggregator(config: JsObject) extends Aggregator {
  val cfg = config.as[Config]
  
  val resultKey = "[SUM]"
  
  override def name = "SimpleSumAggregator"
   
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceAggregated] =
    dataEnumerator.run(iteratee).map { sum =>
      SliceAggregated(
        slice,
        results = Map(resultKey -> sum)
      )
    }
    
  protected def iteratee(implicit ec: ExecutionContext) = IterateeUtils.wrapExceptionToError(
    Iteratee.fold(BigDecimal(0)) { (state: BigDecimal, json: JsObject) => 
      AggregatorUtils.numberValues(json\(cfg.fieldName)).foldLeft(state) { (rez, v) => 
        rez + v    
      }
    }
  )
  
  
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceAggregated]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(_ + _) _
    merger(slices.map(_.results))
  } 
  
}