package slogger.model.specification.extraction

import com.github.nscala_time.time.Imports._
import slogger.model.processing.Slice


case class SlicingSpecs(
  sliceDuration: Duration,
  snapTo: DateTime = new DateTime(0L),
  
  enabled: Boolean = true
) {
  
  def getSlicesFor(interval: Interval): Stream[Slice] = {    
    //find first slice start date
    val slice1start = 
      if (snapTo < interval.getStart()) {      
        val k = (interval.getStart().getMillis() - snapTo.getMillis()) / sliceDuration.getMillis()
        snapTo + k * sliceDuration.getMillis()      
      } else if (snapTo == interval.getStart()) {
        snapTo
      } else /* snapTo > interval.getStart() */ {
        val k = (snapTo.getMillis() - interval.getStart().getMillis()) / sliceDuration.getMillis()
        snapTo - (k + 1) * sliceDuration.getMillis()
      }
    
    
    def s(startTime: DateTime): Stream[Slice] = {
      val endTime = startTime + sliceDuration
      val complete = startTime >= interval.getStart() && endTime <= interval.getEnd()
      val slice = Slice(
        max(startTime, interval.start), 
        min(endTime, interval.end), 
        complete
      )
      
      if (endTime < interval.getEnd()) {
        slice #:: s(endTime)
      } else {
        Stream(slice)
      }
    }
    
    s(slice1start)
  }
  
  protected def max(d1: DateTime, d2: DateTime): DateTime = if (d1 > d2) d1 else d2
  
  protected def min(d1: DateTime, d2: DateTime): DateTime = if (d1 < d2) d1 else d2
}