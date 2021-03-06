package test.processing

import slogger.services.processing.extraction.DataExtractorDaoMongo
import slogger.services.processing.extraction.DataExtractorImpl
import slogger.services.processing.extraction.DataExtractor
import slogger.services.processing.extraction.DataExtractorDao
import slogger.model.specification.extraction.ExtractionSpecs
import play.api.libs.json.Json
import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.LastPeriod
import com.github.nscala_time.time.Imports._
import slogger.model.specification.extraction.TimeLimits


class DataExtractorTest extends BaseDaoTest {

  val dao: DataExtractorDao = new DataExtractorDaoMongo(dbProvider)
  
  val extractor: DataExtractor = new DataExtractorImpl(dao)
  
  it should "work" in {
    val specs = ExtractionSpecs(
      filter = None,
      projection = None,
      timeLimits = TimeLimits.forLast(TimePeriod.Month),
      slicing = None
    )
    
    val rez = extractor.extract(specs, DateTime.now).head
  }
}