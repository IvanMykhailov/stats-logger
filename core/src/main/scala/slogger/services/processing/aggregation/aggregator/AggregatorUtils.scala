package slogger.services.processing.aggregation.aggregator

import play.api.libs.json._


object AggregatorUtils {
  
  def values(js: JsValue): Seq[String] = js match {
    case JsArray(values) => values.map(_.toString)
    case _: JsObject => Seq()
    case _: JsUndefined => Seq()
    case JsNull => Seq()
    case JsString(s) => Seq(s)
    case v: JsValue => Seq(v.toString)
  }
    
  def merge(f: (BigDecimal, BigDecimal) => BigDecimal)(slices: Seq[Map[String, BigDecimal]]): Map[String, BigDecimal] = {
    val mapMerger = mergeMaps(f) _
    slices.reduce(mapMerger)    
  }
  
  def mergeMaps(f: (BigDecimal, BigDecimal) => BigDecimal)(m1: Map[String, BigDecimal], m2: Map[String, BigDecimal]): Map[String, BigDecimal] = {
    (m1.toSeq ++ m2.toSeq).groupBy(_._1).mapValues { seq => 
      val values = seq.map(_._2)
      values.reduce(f)
    }
  }
}