package test.processing.calc

import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.ExtractionSpecs
import play.api.libs.json._
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import org.joda.time.DateTime
import slogger.services.processing.CalculatorContext
import slogger.model.specification.CalculationSpecs
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregators.onefield.SumAggregator
import slogger.services.processing.aggregation.aggregators.onefield
import slogger.model.processing.StatsResult
import slogger.model.processing.SliceResult
import slogger.services.processing.history.StatsResultProvider
import slogger.services.processing.history.StatsResultProviderStub
import java.util.UUID


class ReuseOldResultsInCalculationTest extends BaseCalculationTest {

  
  def calculator(specs: CalculationSpecs, statsResult: StatsResult) = {   
    val context = new CalculatorContext(dbProvider) {
      override lazy val statsResultProvider: StatsResultProvider = new StatsResultProviderStub(specs, statsResult)
    }
    context.calculator
  }
  
  
  def extractionSpecs(fieldName: String, period: TimePeriod.Value = TimePeriod.Hour) = ExtractionSpecs(
    filter = None,
    projection = Some(Json.obj(fieldName -> 1)),
    timeLimits = TimeLimits.specific(referenceCalcInterval),
    slicing = Some(SlicingSpecs(
      sliceDuration = TimePeriod.duration(period),
      snapTo = new DateTime(1493L)
    )),
    customCollectionName = Some("xlogs")
  ) 

  
  behavior of "Calculator"
  
  
  it should "reuse old calculation" in {
    val specs = CalculationSpecs(
      id = UUID.randomUUID().toString(),
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
      total = None
    )
    
    val calc = calculator(specs, oldStatRez)
    
    val rez = twait(calc.calculate(specs, DateTime.now))
    println(rez.statsError)
    rez.statsResult.get.total.get shouldBe (correctRez_AggregationSumTotal)
    
    rez.metaStats.reusedSlices shouldBe (8)
  }
}