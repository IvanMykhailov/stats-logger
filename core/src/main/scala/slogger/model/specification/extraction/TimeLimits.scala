package slogger.model.specification.extraction

import slogger.model.common.TimePeriod
import com.github.nscala_time.time.Imports._
import slogger.model.common.TimePeriod


sealed trait TimeLimits {
  def interval(now: DateTime = DateTime.now): Interval
}


object TimeLimits {
  def apply(interval: Interval) = StartEndTime(interval.start, interval.end)
  def apply(timePeriod: TimePeriod.Value) = LastPeriod(timePeriod)
}


case class LastPeriod(
  forLastPeriod: TimePeriod.Value  
) extends TimeLimits {
  override def interval(now: DateTime = DateTime.now): Interval = 
    new Interval(
      now.minus(TimePeriod.duration(forLastPeriod)),
      now
    )
}


case class StartEndTime(
  startTime: DateTime,
  endTime: DateTime
) extends TimeLimits {
  override def interval(now: DateTime = DateTime.now): Interval = new Interval(startTime, endTime)
}
