package slogger.model.specification.extraction

import play.api.libs.json.JsObject
import com.github.nscala_time.time.Imports._
import slogger.model.common.TimePeriod


case class DataExtraction(
  filter: JsObject,
  projection: JsObject,
  timeLimits: TimeLimits,
  slicing: Option[Slicing]
) {
  def isSlicingEnabled = slicing.map(_.enabled).getOrElse(false)
}


