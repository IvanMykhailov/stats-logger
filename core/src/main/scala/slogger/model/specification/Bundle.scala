package slogger.model.specification

import slogger.model.specification.extraction.DataExtraction
import slogger.model.specification.aggregation.Aggregation


case class Bundle(
  extraction: DataExtraction,
  aggregation: Aggregation
)