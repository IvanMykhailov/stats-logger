package test.processing.calc

import slogger.model.specification.Bundle
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


class CalculationPlainTest extends BaseCalculationTest {

  def extractionSpecs(fieldName: String) = ExtractionSpecs(
    filter = None,
    projection = Some(Json.obj(fieldName -> 1)),
    timeLimits = TimeLimits(referenceCalcInterval),
    slicing = Some(SlicingSpecs(
      sliceDuration = TimePeriod.duration(TimePeriod.Hour)
    )),
    customCollectionName = Some("xlogs")
  ) 
  
  behavior of "Calculator"
  
  it should "calculate counts" in {
    
    val specs = Bundle(
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
    val specs = Bundle(
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
    val specs = Bundle(
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[AverageAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    check(correctRez_AggregationAverageTotal)(rez.total.get)    
  }
}