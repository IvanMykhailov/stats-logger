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
import slogger.services.processing.aggregation.aggregators.FoldAggregator



class CountUniqAggregator(config: JsObject) extends FoldAggregator[Set[String]] {
  val cfg = config.as[Config]
  
  val resultKey = "[UNIQUE_COUNT]"
  
  override def name = "SimpleCountUniqueAggregator"
    
  
  //Slice aggregation
  protected def foldInitState = Set.empty
  
  protected def folder(state: Set[String], json: JsObject) = AggregatorUtils.stringValues(cfg.extractField(json)).foldLeft(state)(_+_)
  
  protected def resultMapper(slice: Slice, valueVariants: Set[String]) = 
    SliceResult(
      slice,
      results = Map(resultKey -> valueVariants.size)
    )
    
   
}