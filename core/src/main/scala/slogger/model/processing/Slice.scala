package slogger.model.processing

import org.joda.time.DateTime

case class Slice(
  start: DateTime,
  end: DateTime,
  complete: Boolean = true
)