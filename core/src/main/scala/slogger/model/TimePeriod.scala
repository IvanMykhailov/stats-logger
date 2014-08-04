package slogger.model

import com.github.nscala_time.time.Imports._

object TimePeriod extends Enumeration {
  type TimePeriod = Value
  val Minute, TenMinutes, Hour, Day, Week, Month = Value
  
  def parse(s: String) = values.find(_.toString.toLowerCase() == s.toLowerCase())
  
  def parseStrict(s: String): Value = parse(s).getOrElse(
    throw new Exception(s"Incorrect TimePeriod value '$s'. Allowed values: " + values.mkString(", "))
  )
  
  def duration(p: TimePeriod): Duration = p match {
    case Minute     => new Duration(           1L * 60 * 1000)
    case TenMinutes => new Duration(          10L * 60 * 1000)
    case Hour       => new Duration(      1L * 60 * 60 * 1000)
    case Day        => new Duration(1L  * 24 * 60 * 60 * 1000)
    case Week       => new Duration(7L  * 24 * 60 * 60 * 1000)
    case Month      => new Duration(31L * 24 * 60 * 60 * 1000)
    
  }
}