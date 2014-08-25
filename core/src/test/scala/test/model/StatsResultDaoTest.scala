package test.model

import play.api.libs.json._
import slogger.model.common.TimePeriod
import slogger.model.processing.StatsResult
import slogger.model.specification.CalculationSpecs
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.specification.extraction.TimeLimits
import test.processing.BaseDaoTest
import org.joda.time.DateTime
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregators.onefield.CountAggregator
import slogger.model.BsonHandlers
import reactivemongo.bson.BSONDocument
import org.joda.time.Duration
import reactivemongo.bson.Macros
import org.joda.time.Interval
import slogger.services.processing.history.CalculationResultDaoMongo
import slogger.services.processing.history.CalculationResultDao
import slogger.model.processing.CalculationResult
import slogger.model.processing.CalculationMetaStats
import java.util.UUID


class StatsResultDaoTest extends BaseDaoTest {
  
  val dao: CalculationResultDao = new CalculationResultDaoMongo(dbProvider)

  
  def newCalculationSpecs() = {
    val extraction = ExtractionSpecs(
      filter = None,
      projection = Some(Json.obj("testFields" -> 1)),
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
    
    CalculationSpecs(
      extraction,
      aggregation,
      id = UUID.randomUUID().toString()
    )
  }
  
  
  def newCalcResult(specs: CalculationSpecs) = {
    val statsRez = StatsResult(
      lines = Seq.empty,
      total = None
    )    
    CalculationResult(
      calculationSpecs = specs,
      calculatedAt = DateTime.now,
      metaStats = CalculationMetaStats(1, 1, new Duration(1)),
      statsResult = Some(statsRez)
    )
  }
  
  
  "BigDecimal" should "be equal" in {
    val bd1 = BigDecimal("1.0")
    val bd2 = BigDecimal("1")
    
    bd1 should be (bd2)
    
  }
  
  
  "SlicingSpecs" should "be (de)serialized" in {
    val s = newCalculationSpecs().extraction.slicing.get    
    val bson = BsonHandlers.SlicingSpecsHandler.write(s)
    val loaded = BsonHandlers.SlicingSpecsHandler.read(bson)    
    loaded should be (s)   
  }
  
  
  "CalculationSpecs" should "be (de)serialized" in {
    val b = newCalculationSpecs()    
    val bson = BsonHandlers.CalculationSpecsHandler.write(b)    
    val loaded = BsonHandlers.CalculationSpecsHandler.read(bson)    
    loaded should be (b)    
  }
  
  
  "CalculationResultDao" should "save stats" in {
    val f = dao.save(newCalcResult(newCalculationSpecs()))
    twait(f)
  }
  
  
  it should "load saved stats by CalculationSpecs" in {
    val calcSpecs = newCalculationSpecs()
    val calcRez = newCalcResult(calcSpecs)
    twait(dao.save(calcRez))    
    val loaded = twait(dao.findBySpecs(calcSpecs))    
    loaded.get should be (calcRez)
  }
  
  
  it should "not load stats if CalculationSpecs is changed, even if id is same" in {
    val calcSpecs = newCalculationSpecs()
    val calcRez = newCalcResult(calcSpecs)
    
    val dcalcSpecs = calcSpecs.copy(
      extraction = calcSpecs.extraction.copy(
        customCollectionName = Some("test2")
      )    
    )    
    twait(dao.save(calcRez))    
    val loaded = twait(dao.findBySpecs(dcalcSpecs))    
    loaded should be (None)
  }
  
  
  it should "load saved stats by CalculationSpecs even if time period is different" in {    
    val calcSpecs = newCalculationSpecs()
    val calcRez = newCalcResult(calcSpecs)
    
    val dcalcSpecs = calcSpecs.copy(
      extraction = calcSpecs.extraction.copy(
        timeLimits = TimeLimits(new Interval(DateTime.now, DateTime.now))
      )    
    )    
    twait(dao.save(calcRez))    
    val loaded = twait(dao.findBySpecs(dcalcSpecs))    
    loaded.get should be (calcRez)    
  }
}

