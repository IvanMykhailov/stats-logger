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
class MaxAggregator(config: JsObject) extends FoldAggregator[Map[String, BigDecimal]] {
  val cfg = config.as[Config]
  
  override def name = "TwofieldMaxAggregator"

    
  //Slice aggregation
  protected def foldInitState = Map.empty
  
  protected def folder(state: Map[String, BigDecimal], json: JsObject) = 
    AggregatorUtils.stringValues(cfg.extractKeyField(json)).foldLeft(state) { (rez, key) =>
      val values = AggregatorUtils.numberValues(cfg.extractValueField(json))
      if (values.size > 0) {
        val recordMax = values.max
        rez.get(key) match {
          case None => rez + (key -> recordMax)
          case Some(cur) if recordMax > cur => rez + (key -> recordMax)
          case _ => rez
        }
      } else {
        rez
      }
    }
  
  protected def resultMapper(slice: Slice, rez: Map[String, BigDecimal]) = 
    SliceResult(
      slice,
      results = rez
    )
    
  
  //Total aggregation
  override def isSliceMergingSupported = true
  
  override def mergeSlices(slices: Seq[SliceResult]): Map[String, BigDecimal] = {
    val merger = AggregatorUtils.merge(max) _
    merger(slices.map(_.results))
  } 
  
  
  protected def max(a: BigDecimal, b: BigDecimal): BigDecimal = if (a > b) a else b
  
  protected def max(aopt: Option[BigDecimal], b: BigDecimal): BigDecimal = aopt match {
    case None => b
    case Some(a) => max(a, b)
  }
    
}