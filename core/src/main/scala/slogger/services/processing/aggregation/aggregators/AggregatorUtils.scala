package slogger.services.processing.aggregation.aggregators

import play.api.libs.json._
import scala.util.Try


object AggregatorUtils {
  
  def stringValues(js: JsValue): Seq[String] = js match {
    case JsArray(values) => values.flatMap {
      case JsString(s) => Iterable(s)
      case _: JsObject => Iterable.empty
      case _: JsUndefined => Iterable.empty
      case _: JsArray => Iterable.empty
      case v: JsValue => Iterable(v.toString)      
    }
    case JsString(s) => Seq(s)
    
    case _: JsObject => Seq()
    case _: JsUndefined => Seq()
    case JsNull => Seq()
    
    case v: JsValue => Seq(v.toString)
  }
  
  
  def numberValues(js: JsValue): Seq[BigDecimal] = js match {
    case JsNumber(v) => Seq(v)
    case JsString(s) => stringToBigDecimal(s).toSeq
    case JsArray(values) => values.flatMap {
      case JsNumber(v) => Iterable(v)
      case JsString(s) => stringToBigDecimal(s)
      case _ => Iterable.empty
    }
    case _ => Seq()
  }
  
  def stringToBigDecimal(s: String): Option[BigDecimal] = Try(BigDecimal(s)).toOption
  
  
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