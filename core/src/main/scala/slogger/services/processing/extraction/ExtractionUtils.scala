package slogger.services.processing.extraction

import play.api.libs.json.JsValue
import scala.collection.JavaConverters._


object ExtractionUtils {
  
  /**
   * Extract value by path from JsValue (usually JsObject)
   * path - dot-separated path to final filed, like "sensor1.data.time"
   */
  def extractByPath(json: JsValue, path: String): JsValue = {
    def ex(path: List[String], json: JsValue): JsValue = path match {
      case Nil => json
      case elem::Nil => json\elem
      case elem::tail => ex(tail, json\elem)
    }    
    ex(path.split("\\.").toList, json)
  }
}