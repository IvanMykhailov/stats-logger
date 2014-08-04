package slogger.model.specification.extraction

import play.api.libs.json.JsObject
import org.joda.time.DateTime
import slogger.model.TimePeriod
import slogger.model.TimePeriod


case class DataExtraction(
  filter: JsObject,
  projection: JsObject,
  timeLimits: TimeLimits,
  slicing: Option[Slicing]
) {
  def isSlicingEnabled = slicing.map(_.enabled).getOrElse(false)
}


