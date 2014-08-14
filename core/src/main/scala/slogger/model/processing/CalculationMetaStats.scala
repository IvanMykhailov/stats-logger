package slogger.model.processing

import org.joda.time.Duration


case class CalculationMetaStats (
  processedDocuments: Long,
  reusedSlices: Long,
  processingTime: Duration
)