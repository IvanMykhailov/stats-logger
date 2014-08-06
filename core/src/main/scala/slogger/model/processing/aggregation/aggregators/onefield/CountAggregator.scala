package slogger.model.processing.aggregation.aggregators.onefield

import slogger.model.processing.aggregation.Aggregator
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import scala.concurrent.Future


/**
 * Return count of each value for all found values in specified field.
 * Field can be array of simple types. In that case each array element is count as separate value 
 */
class CountAggregator(config: JsObject) extends Aggregator {
  val cfg = config.as[Config]
  
  override def name = "SimpleCountAggregator"
  override def aggregate(enumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[Map[String, BigDecimal]] = enumerator.run(iteratee)
  
  
  
  def iteratee(implicit ec: ExecutionContext) = Iteratee.fold(Map.empty[String, BigDecimal]){ (state: Map[String, BigDecimal], json: JsObject) => 
    values(json).foldLeft(state){ (rez, v) => 
      val count = rez.getOrElse(v, BigDecimal(0)) + 1
      rez + (v -> count)      
    }
  }
  
  
  def values(js: JsValue): Seq[String] = js match {
    case JsArray(values) => values.map(_.toString)
    case obj: JsObject => Seq()
    case v: JsValue => Seq(v.toString)
  }
}