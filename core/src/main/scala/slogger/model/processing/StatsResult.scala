package slogger.model.processing

case class StatsResult(
  lines: Seq[(Slice, Map[String, BigDecimal])],  
  total: Option[Map[String, BigDecimal]]
)