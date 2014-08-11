package slogger.model.specification

import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.aggregation.AggregationSpecs
import java.util.UUID


case class SpecsBundle(  
  extraction: ExtractionSpecs,
  aggregation: AggregationSpecs,
  
  id: UUID =  UUID.randomUUID
) { 
  def equalsIgnoreTime(that: SpecsBundle): Boolean =
    this.extraction.copy(timeLimits = that.extraction.timeLimits) == that.extraction &&
    this.aggregation == that.aggregation
}