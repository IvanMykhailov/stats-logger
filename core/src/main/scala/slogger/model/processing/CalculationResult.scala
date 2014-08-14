package slogger.model.processing

import org.joda.time.DateTime
import slogger.model.specification.CalculationSpecs
import org.joda.time.Duration


case class CalculationResult(  
  calculationSpecs: CalculationSpecs,
  calculatedAt: DateTime,
  metaStats: CalculationMetaStats,
  
  statsResult: Option[StatsResult] = None,
  statsError: Option[StatsError] = None
) {
  if (statsResult.isEmpty == statsError.isEmpty) throw new IllegalArgumentException("Specify only one of statsResult or statsError")
  
  def isError = statsError.isDefined
}