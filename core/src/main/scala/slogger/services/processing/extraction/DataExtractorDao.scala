package slogger.services.processing.extraction

import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import play.api.libs.iteratee.Enumerator


trait DataExtractorDao {
  
  def load(times: Interval, filter: Option[JsObject], projection: Option[JsObject]): Enumerator[JsObject]
}