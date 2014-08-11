package test.model

import play.api.libs.json._
import slogger.model.common.TimePeriod
import slogger.model.processing.StatsResult
import slogger.model.specification.SpecsBundle
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.specification.extraction.TimeLimits
import slogger.services.processing.history.StatsResultDao
import slogger.services.processing.history.StatsResultDaoMongo
import test.processing.BaseDaoTest
import org.joda.time.DateTime
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregators.onefield.CountAggregator
import slogger.model.BsonHandlers
import reactivemongo.bson.BSONDocument
import org.joda.time.Duration
import reactivemongo.bson.Macros
import org.joda.time.Interval


class StatsResultDaoTest extends BaseDaoTest {
  
  val dao: StatsResultDao = new StatsResultDaoMongo(dbProvider)

  def newSpecsBundle() = {
    val extraction = ExtractionSpecs(
      filter = None,
      projection = Some(Json.obj("testFiels" -> 1)),
      timeLimits = TimeLimits(TimePeriod.Hour),
      slicing = Some(SlicingSpecs(
        sliceDuration = TimePeriod.duration(TimePeriod.Minute)
      )),
      customCollectionName = Some("xlogs")
    )
    val aggregation = AggregationSpecs(
      aggregatorClass = classOf[CountAggregator].getName(),
      config = Json.obj()
    )  
    
    SpecsBundle(
      extraction,
      aggregation
    )
  }
  
  def newStatsResult(specs: SpecsBundle) = StatsResult(
    lines = Seq.empty,
    total = None,
    calcTime = DateTime.now,
    bundle = Some(specs)
  )
  
  "BigDecimal" should "be equal" in {
    val bd1 = BigDecimal("1.0")
    val bd2 = BigDecimal("1")
    
    bd1 should be (bd2)
    
  }
  
  "SlicingSpecs" should "be (de)serialized" in {
    val s = newSpecsBundle().extraction.slicing.get    
    val bson = BsonHandlers.SlicingSpecsHandler.write(s)
    val loaded = BsonHandlers.SlicingSpecsHandler.read(bson)    
    loaded should be (s)   
  }
  
  "SpecsBundle" should "be (de)serialized" in {
    val b = newSpecsBundle()    
    val bson = BsonHandlers.SpecsBundleHandler.write(b)    
    val loaded = BsonHandlers.SpecsBundleHandler.read(bson)    
    loaded should be (b)    
  }
  
  "StatsResultDao" should "save stats" in {
    val f = dao.save(newStatsResult(newSpecsBundle()))
    twait(f)
  }
  
  it should "load saved stats by bundle" in {
    val bundle = newSpecsBundle()
    val statsRez = newStatsResult(bundle)
    twait(dao.save(statsRez))    
    val loaded = twait(dao.findByBundle(bundle))    
    loaded.get should be (statsRez)
  }
  
  it should "not load stats if bundle is changed, even if id is same" in {
    val bundle = newSpecsBundle()
    val statsRez = newStatsResult(bundle)
    
    val dbundle = bundle.copy(
      extraction = bundle.extraction.copy(
        customCollectionName = Some("test2")
      )    
    )    
    twait(dao.save(statsRez))    
    val loaded = twait(dao.findByBundle(dbundle))    
    loaded should be (None)
  }
  
  it should "load saved stats by bundle even if time period is different" in {
    
    val bundle = newSpecsBundle()
    val statsRez = newStatsResult(bundle)
    
    val dbundle = bundle.copy(
      extraction = bundle.extraction.copy(
        timeLimits = TimeLimits(new Interval(DateTime.now, DateTime.now))
      )    
    )    
    twait(dao.save(statsRez))    
    val loaded = twait(dao.findByBundle(dbundle))    
    loaded.get should be (statsRez)    
  }
}
