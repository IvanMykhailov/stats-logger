package slogger.model.specification.extraction

import play.api.libs.json.JsObject
import com.github.nscala_time.time.Imports._
import slogger.model.common.TimePeriod


case class ExtractionSpecs(
  filter: Option[JsObject],
  projection: Option[JsObject],
  timeLimits: TimeLimits,
  slicing: Option[SlicingSpecs],
  customCollectionName: Option[String] = None
) {
  def isSlicingEnabled = slicing.map(_.enabled).getOrElse(false)
}


