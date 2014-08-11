package test.processing.calc

import slogger.model.specification.SpecsBundle
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.common.TimePeriod
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregators.onefield.CountAggregator
import play.api.libs.json._
import slogger.services.processing.aggregation.aggregators.onefield
import slogger.services.processing.aggregation.aggregators.onefield.SumAggregator
import slogger.services.processing.aggregation.aggregators.onefield.AverageAggregator
import slogger.services.processing.aggregation.aggregators.onefield.CountUniqAggregator


class CalculationPlainTest extends BaseCalculationTest {

  def extractionSpecs(fieldName: String, period: TimePeriod.Value = TimePeriod.Hour) = ExtractionSpecs(
    filter = None,
    projection = Some(Json.obj(fieldName -> 1)),
    timeLimits = TimeLimits(referenceCalcInterval),
    slicing = Some(SlicingSpecs(
      sliceDuration = TimePeriod.duration(period)
    )),
    customCollectionName = Some("xlogs")
  ) 
  
  behavior of "Calculator"
  
  
  it should "calculate counts" in {
    
    val specs = SpecsBundle(
      extraction = extractionSpecs("level"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    rez.total.get shouldBe (correctRez_AggregationCountTotal)
  }
  
  
  it should "calculate sum" in {
    val specs = SpecsBundle(
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    )
    
    val rez = calculator.calculate(specs)
    rez.total.get shouldBe (correctRez_AggregationSumTotal)
  }
  
  
  it should "calculate average" in {
    val specs = SpecsBundle(
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[AverageAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    check(correctRez_AggregationAverageTotal)(rez.total.get)    
  }
  
  
  it should "calculate same totals independently from slices lenght" in {
    val aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    
    val specs = SpecsBundle(
      extractionSpecs("characterLevel", TimePeriod.Minute),
      aggregation 
    )
    val rez = calculator.calculate(specs)
    rez.total.get shouldBe (correctRez_AggregationSumTotal)
    
    val specs2 = SpecsBundle(
      extractionSpecs("characterLevel", TimePeriod.Day),
      aggregation 
    )
    val rez2 = calculator.calculate(specs)
    rez2.total.get shouldBe (correctRez_AggregationSumTotal)
    
  }
  
    
  it should "calculate unique" in {
    val specs = SpecsBundle(
      extraction = extractionSpecs("level", TimePeriod.Month),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountUniqAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)    
    rez.lines(0).results shouldBe (correctRez_AggregationUniqueTotal)
  }
}