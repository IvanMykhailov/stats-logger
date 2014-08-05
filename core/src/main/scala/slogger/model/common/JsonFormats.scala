package slogger.model.common

import play.api.libs.json._
import com.github.nscala_time.time.Imports._


trait JsonFormats {
 
  implicit val TimePeriodFormat: Format[TimePeriod.Value] = enumFormat(TimePeriod)
  
  
  def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = new Format[E#Value] {    
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) => {
        try {
          JsSuccess(enum.values.find(_.toString.toLowerCase() == s.toLowerCase()).get)
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      }
      case _ => JsError("String value expected")
    }    
    def writes(v: E#Value): JsValue = JsString(v.toString)    
  }
  
  
  implicit val JodaDurationFormat = new OFormat[Duration] {    
    def reads(json: JsValue): JsResult[Duration] = 
      for {
        millis <- (__ \ 'duration).read[Long].reads(json)
      } yield {
        new Duration(millis) 
      }    
    def writes(d: Duration): JsObject = Json.obj(
      "duration" -> d.getMillis()
    )
  }
 
}


object JsonFormats extends JsonFormats