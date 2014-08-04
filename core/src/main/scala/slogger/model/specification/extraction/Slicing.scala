package slogger.model.specification.extraction

import com.github.nscala_time.time.Imports._
import slogger.model.processing.Slice


case class Slicing(
  snapTo: DateTime = new DateTime(0L),
  sliceDuration: Duration, 
    
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
      val complete = endTime <= interval.getEnd()
      val slice = Slice(startTime, endTime, complete)
      
      if (endTime < interval.getEnd()) {
        slice #:: s(endTime)
      } else {
        Stream(slice)
      }
    }
    
    s(slice1start)
  }
}