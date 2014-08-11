package test.processing.calc

import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.ExtractionSpecs
import play.api.libs.json._
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import org.joda.time.DateTime
import slogger.services.processing.CalculatorContext
import slogger.services.processing.history.StatsResultDaoInmemory
import slogger.model.specification.SpecsBundle
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregators.onefield.SumAggregator
import slogger.services.processing.aggregation.aggregators.onefield
import slogger.model.processing.StatsResult
import slogger.model.processing.SliceResult


class ReuseOldResultsInCalculationTest extends BaseCalculationTest {

  val rezDao = new StatsResultDaoInmemory
  
  val context = new CalculatorContext(dbProvider) {
    override lazy val statsResultsDao = rezDao
  }
  
  override val calculator = context.calculator
  
  def extractionSpecs(fieldName: String, period: TimePeriod.Value = TimePeriod.Hour) = ExtractionSpecs(
    filter = None,
    projection = Some(Json.obj(fieldName -> 1)),
    timeLimits = TimeLimits(referenceCalcInterval),
    slicing = Some(SlicingSpecs(
      sliceDuration = TimePeriod.duration(period),
      snapTo = new DateTime(1493L)
    )),
    customCollectionName = Some("xlogs")
  ) 
  
  behavior of "Calculator"

  
  it should "reuse old calculation" in {
    val specs = SpecsBundle(
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    )
    
    val oldSlices = SumCalculationSlices.toSeq.map { case (s, v) =>
      SliceResult(s, v)
    }
    
    val oldStatRez = StatsResult(
      lines = oldSlices,  
      total = None,
      calcTime = new DateTime(0),
      bundle = Some(specs)    
    )
    rezDao.save(oldStatRez)
    
    val rez = twait(calculator.calculate(specs))
    rez.total.get shouldBe (correctRez_AggregationSumTotal)    
  }
}