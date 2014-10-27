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
class CountUniqAggregator(config: JsObject) extends FoldAggregator[Map[String, Set[String]]] {
  val cfg = config.as[Config]
  
  override def name = "TwofieldCountUniqAggregator"

    
  //Slice aggregation
  protected def foldInitState = Map.empty
  
  protected def folder(state: Map[String, Set[String]], json: JsObject) = 
    AggregatorUtils.stringValues(cfg.extractKeyField(json)).foldLeft(state) { (rez, key) =>
      val values = AggregatorUtils.stringValues(cfg.extractValueField(json))    
      val newValuesSet = rez.getOrElse(key, Set.empty) ++ values 
      rez + (key -> newValuesSet)
    }
  
  protected def resultMapper(slice: Slice, rez: Map[String, Set[String]]) = 
    SliceResult(
      slice,
      results = rez.mapValues(_.size)
    )
  
}