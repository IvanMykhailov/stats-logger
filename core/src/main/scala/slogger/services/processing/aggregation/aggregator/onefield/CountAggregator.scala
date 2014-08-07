package slogger.services.processing.aggregation.aggregator.onefield

import slogger.services.processing.aggregation.Aggregator
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.concurrent.Future
import slogger.services.processing.aggregation.aggregator.AggregatorUtils
import slogger.model.processing.Slice
import slogger.model.processing.SliceAggregated


/**
 * Return count of each value for all found values in specified field.
 * Field can be array of simple types. In that case each array element is count as separate value 
 */
class CountAggregator(config: JsObject) extends Aggregator {
  val cfg = config.as[Config]
  
  override def name = "SimpleCountAggregator"
   
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceAggregated] =
    dataEnumerator.run(iteratee).map { results =>  
      SliceAggregated(
        slice,
        results,
        meta = Map.empty
      )
    }
    
  protected def iteratee(implicit ec: ExecutionContext) = Iteratee.fold(Map.empty[String, BigDecimal]){ (state: Map[String, BigDecimal], json: JsObject) => 
    AggregatorUtils.values(json\(cfg.fieldName)).foldLeft(state){ (rez, v) => 
      val count = rez.getOrElse(v, BigDecimal(0)) + 1
      rez + (v -> count)      
    }
  }
  
  
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceAggregated]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(_ + _) _
    merger(slices.map(_.results))
  } 
  
}