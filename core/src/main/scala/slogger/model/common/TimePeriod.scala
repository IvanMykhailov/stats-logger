package slogger.model.common

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants

object TimePeriod extends Enumeration {
  type TimePeriod = Value
  val Minute, TenMinutes, Hour, Day, Week, Month = Value
  
  def parse(s: String) = values.find(_.toString.toLowerCase() == s.toLowerCase())
  
  def parseStrict(s: String): Value = parse(s).getOrElse(
    throw new Exception(s"Incorrect TimePeriod value '$s'. Allowed values: " + values.mkString(", "))
  )
  
  def duration(p: TimePeriod): Duration = p match {
    case Minute     => new Duration(DateTimeConstants.MILLIS_PER_MINUTE)
    case TenMinutes => new Duration(DateTimeConstants.MILLIS_PER_MINUTE * 10)
    case Hour       => new Duration(DateTimeConstants.MILLIS_PER_HOUR)
    case Day        => new Duration(DateTimeConstants.MILLIS_PER_DAY)
    case Week       => new Duration(DateTimeConstants.MILLIS_PER_WEEK)
    case Month      => new Duration(DateTimeConstants.MILLIS_PER_DAY.longValue * 30) 
  }
}