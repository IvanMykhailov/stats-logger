package slogger.model.specification

import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.aggregation.AggregationSpecs


case class Bundle(
  extraction: ExtractionSpecs,
  aggregation: AggregationSpecs
)