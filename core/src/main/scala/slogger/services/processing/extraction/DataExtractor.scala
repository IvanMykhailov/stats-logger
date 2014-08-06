package slogger.services.processing.extraction

import slogger.model.specification.extraction.DataExtraction
import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import slogger.model.processing.Slice
import slogger.model.specification.extraction.Slicing
import play.api.libs.iteratee.Enumerator


trait DataExtractor {

  def extract(spec: DataExtraction, now: DateTime = DateTime.now): Seq[(Slice, Enumerator[JsObject])]
}


class DataExtractorImpl(
  dao: DataExtractorDao    
) extends DataExtractor {
  
  override def extract(specs: DataExtraction, now: DateTime = DateTime.now): Seq[(Slice, Enumerator[JsObject])] = {
    val interval = specs.timeLimits.interval(now)
    val slicing = if (specs.isSlicingEnabled) {
      specs.slicing.get
    } else {
      //one virtual slice for whole period
      Slicing(
        snapTo = interval.end,
        sliceDuration = interval.toDuration()
      )
    }
    //force laziness to avoid simultaneous db requests
    slicing.getSlicesFor(interval).view.map { slice =>
      val data = dao.load(slice.toInterval, specs.filter, specs.projection, specs.customCollectionName)
      (slice, data)
    }
  }
}