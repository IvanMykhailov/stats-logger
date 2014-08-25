package slogger.services.processing.extraction

import slogger.model.specification.extraction.ExtractionSpecs
import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import slogger.model.processing.Slice
import slogger.model.specification.extraction.SlicingSpecs
import play.api.libs.iteratee.Enumerator
import org.slf4j.LoggerFactory


trait DataExtractor {

  def extract(spec: ExtractionSpecs, now: DateTime = DateTime.now): Seq[(Slice, Enumerator[JsObject])]
}


class DataExtractorImpl(
  dao: DataExtractorDao    
) extends DataExtractor {
  val log = LoggerFactory.getLogger("slogger")
  
  override def extract(specs: ExtractionSpecs, now: DateTime = DateTime.now): Seq[(Slice, Enumerator[JsObject])] = {
    val interval = specs.timeLimits.interval(now)
    log.debug(s"Data extraction interval ${interval}")
    
    val slicing = if (specs.isSlicingEnabled) {
      specs.slicing.get
    } else {
      //one virtual slice for whole period
      SlicingSpecs(
        snapTo = interval.end,
        sliceDuration = interval.toDuration()
      )
    }
    
    slicing.getSlicesFor(interval).map { slice =>
      val data = dao.load(slice.toInterval, specs.filter, specs.projection, specs.customCollectionName)
      (slice, data)
    }
    
  } 
}