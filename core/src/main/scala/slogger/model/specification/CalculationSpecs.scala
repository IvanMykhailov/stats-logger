package slogger.model.specification

import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.aggregation.AggregationSpecs
import java.util.UUID


case class CalculationSpecs(  
  extraction: ExtractionSpecs, 
  aggregation: AggregationSpecs, 
  
  id: String
) { 
  def equalsIgnoreTime(that: CalculationSpecs): Boolean =
    CalculationSpecs.this.extraction.copy(timeLimits = that.extraction.timeLimits) == that.extraction &&
    CalculationSpecs.this.aggregation == that.aggregation
}