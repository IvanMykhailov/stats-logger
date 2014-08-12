package slogger.model.processing

import org.joda.time.DateTime
import slogger.model.specification.SpecsBundle
import org.joda.time.Duration


case class CalculationResult(  
  bundle: SpecsBundle,
  calculatedAt: DateTime,
  
  statsResult: Option[StatsResult] = None,
  statsError: Option[StatsError] = None
) {
  if (statsResult.isEmpty == statsError.isEmpty) throw new IllegalArgumentException("Specify only one of statsResult or statsError")
}