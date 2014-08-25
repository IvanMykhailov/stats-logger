package slogger.model.specification.extraction

import slogger.model.common.TimePeriod
import com.github.nscala_time.time.Imports._
import slogger.model.common.TimePeriod


sealed trait TimeLimits {
  def interval(now: DateTime = DateTime.now): Interval
}


object TimeLimits {
  def specific(interval: Interval) = StartEndTime(interval.start, interval.end)
  def forLast(timePeriod: TimePeriod.Value) = LastPeriod(TimePeriod.duration(timePeriod))
  def forLast(duration: Duration) = LastPeriod(duration)
}


case class LastPeriod(
  forLast: Duration  
) extends TimeLimits {
  override def interval(now: DateTime = DateTime.now): Interval = 
    new Interval(
      now.minus(forLast),
      now
    )
}


case class StartEndTime(
  startTime: DateTime,
  endTime: DateTime
) extends TimeLimits {
  override def interval(now: DateTime = DateTime.now): Interval = new Interval(startTime, endTime)
}
