package test.processing.calc

import com.github.nscala_time.time.Imports._


trait ReferenceResults {

  val correctRez_AggregationCountTotal = Map(
    "ui" -> 22759,
    "error" -> 2143,
    "debug"-> 69,
    "warning" -> 3,
    "info" -> 9848
  )
  
  val correctRez_AggregationAverageTotal = Map(
    "[AVERAGE]" -> 12.5049213476
  )
  
  val correctRez_AggregationSumTotal = Map(
    "[SUM]" -> 420528.0
  )
    
  val referenceCalcInterval = new Interval(
    new DateTime(2012, 1, 10, 2, 0, 0, 0, DateTimeZone.UTC),
    new DateTime(2012, 1, 10, 19, 0, 0, 0, DateTimeZone.UTC)
  )
    
  val wholeDataTimeInterval = new Interval(
    new DateTime("2012-01-09T22:00:01.687Z"),
    new DateTime("2012-01-10T21:59:58.339Z")
  )
}