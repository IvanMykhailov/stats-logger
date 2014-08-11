package test.processing.calc

import com.github.nscala_time.time.Imports._
import slogger.model.processing.Slice


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
  
  val correctRez_AggregationUniqueTotal = Map(
    "[UNIQUE_COUNT]" -> 5
  )
    
  val referenceCalcInterval = new Interval(
    new DateTime(2012, 1, 10, 2, 0, 0, 0, DateTimeZone.UTC),
    new DateTime(2012, 1, 10, 19, 0, 0, 0, DateTimeZone.UTC)
  )
    
  val wholeDataTimeInterval = new Interval(
    new DateTime("2012-01-09T22:00:01.687Z"),
    new DateTime("2012-01-10T21:59:58.339Z")
  )
  
  
  //for snapTo = new DateTime(1493L)
  val SumCalculationSlices = Seq(
    Slice(new DateTime("2012-01-10T02:00:00.000Z"),      new DateTime("2012-01-10T04:00:01.493+02:00"), false) -> Map("[SUM]" -> BigDecimal(8)),
    Slice(new DateTime("2012-01-10T04:00:01.493+02:00"), new DateTime("2012-01-10T05:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(20185.0)),
    Slice(new DateTime("2012-01-10T05:00:01.493+02:00"), new DateTime("2012-01-10T06:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(18881.0)),
    Slice(new DateTime("2012-01-10T06:00:01.493+02:00"), new DateTime("2012-01-10T07:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(25685.0)),
    Slice(new DateTime("2012-01-10T07:00:01.493+02:00"), new DateTime("2012-01-10T08:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(19391.0)),
    Slice(new DateTime("2012-01-10T08:00:01.493+02:00"), new DateTime("2012-01-10T09:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(16340.0)),
    Slice(new DateTime("2012-01-10T09:00:01.493+02:00"), new DateTime("2012-01-10T10:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(20221.0)),
    Slice(new DateTime("2012-01-10T10:00:01.493+02:00"), new DateTime("2012-01-10T11:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(17645.0)),
    Slice(new DateTime("2012-01-10T11:00:01.493+02:00"), new DateTime("2012-01-10T12:00:01.493+02:00"), true)  -> Map("[SUM]" -> BigDecimal(18070.0))   
  )
}