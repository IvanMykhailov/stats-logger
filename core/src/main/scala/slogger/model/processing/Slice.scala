package slogger.model.processing

import com.github.nscala_time.time.Imports._


case class Slice(
  start: DateTime,
  end: DateTime,
  complete: Boolean = true
) {
  def toInterval = new Interval(start, end)
}