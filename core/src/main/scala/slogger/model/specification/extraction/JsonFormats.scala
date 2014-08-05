package slogger.model.specification.extraction

import play.api.libs.json._
import slogger.model.common

trait JsonFormats extends common.JsonFormats {
  
  val SlicingFormat = Json.format[Slicing] 
  
  
  val TimeLimitsFromat = {
    implicit val LastPeriodFormat = Json.format[LastPeriod]
    implicit val StartEndTimeFormat = Json.format[StartEndTime]
    
    new Format[TimeLimits] {
      def reads(json: JsValue): JsResult[TimeLimits] = {
        val lprez = LastPeriodFormat.reads(json)
        val setrez = StartEndTimeFormat.reads(json)
                
        lprez.orElse(setrez) match {
          case rez: JsSuccess[_] => rez
          case rez: JsError => JsError.merge(lprez.asInstanceOf[JsError], setrez.asInstanceOf[JsError])
        }
      }      
      def writes(tl: TimeLimits): JsValue = tl match {
        case v: LastPeriod => Json.toJson(v)
        case v: StartEndTime => Json.toJson(v)
      }
    }
    
  }
}


object JsonFormats extends JsonFormats