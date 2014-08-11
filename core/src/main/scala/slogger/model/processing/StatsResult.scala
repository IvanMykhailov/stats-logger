package slogger.model.processing

import java.util.UUID
import slogger.model.specification.SpecsBundle
import org.joda.time.DateTime


case class StatsResult(  
  lines: Seq[SliceResult],  
  total: Option[Map[String, BigDecimal]],
  calcTime: DateTime,
  
  bundle: Option[SpecsBundle] = None
)