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



class CountUniqAggregator(config: JsObject) extends Aggregator {
  val cfg = config.as[Config]
  
  val resultKey = "[UNIQUE_COUNT]"
  
  override def name = "SimpleCountUniqueAggregator"
   
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceResult] = {
    val rezF = dataEnumerator |>>| iteratee map(IterateeUtils.unwrapErrortoException)
    rezF.map { valueVariants =>  
      SliceResult(
        slice,
        results = Map(resultKey -> valueVariants.size)
      )
    }
  }
    
  protected def iteratee(implicit ec: ExecutionContext) = IterateeUtils.wrapExceptionToError(
    Iteratee.fold(Set.empty[String]) { (state, json: JsObject) => 
      AggregatorUtils.stringValues(json\(cfg.fieldName)).foldLeft(state) { (rez, v) => 
        rez + (v)      
      }
    }
  )
}