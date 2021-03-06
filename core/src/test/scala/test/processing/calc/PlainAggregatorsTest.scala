package test.processing.calc

import slogger.model.specification.CalculationSpecs
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
import org.joda.time.DateTime
import slogger.services.processing.CalculatorContext
import java.util.UUID


class PlainAggregatorsTest extends BaseCalculationTest {

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
  
  
  it should "calculate counts" in {
    
    val specs = CalculationSpecs(
      id = UUID.randomUUID().toString(),
      extraction = extractionSpecs("level"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = twait(calculator.calculate(specs, DateTime.now))
    rez.statsResult.get.total.get shouldBe (correctRez_AggregationCountTotal)
  }
  
  
  it should "calculate sum" in {
    val specs = CalculationSpecs(
      id = UUID.randomUUID().toString(),
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    )
    
    val rez = twait(calculator.calculate(specs, DateTime.now))
    rez.statsResult.get.total.get shouldBe (correctRez_AggregationSumTotal)
  }
  
  
  it should "calculate average" in {
    val specs = CalculationSpecs(
      id = UUID.randomUUID().toString(),
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[AverageAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      ) 
    )
    
    val rez = twait(calculator.calculate(specs, DateTime.now))
    check(correctRez_AggregationAverageTotal)(rez.statsResult.get.total.get)    
  }
  
  
  it should "calculate same totals independently from slices lenght" in {
    Seq(TimePeriod.Minute, TimePeriod.Hour, TimePeriod.Day, TimePeriod.Month).foreach { timePeriod =>
      
      val aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    
      val specs = CalculationSpecs(
        extractionSpecs("characterLevel", timePeriod),
        aggregation,
        id = UUID.randomUUID().toString()
      )
      val rez = twait(calculator.calculate(specs, DateTime.now))
      rez.statsResult.get.total.get shouldBe (correctRez_AggregationSumTotal)
    }
  }
  
    
  it should "calculate unique" in {
    val specs = CalculationSpecs(
      id = UUID.randomUUID().toString(),
      extraction = extractionSpecs("level", TimePeriod.Month),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountUniqAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = twait(calculator.calculate(specs, DateTime.now))    
    rez.statsResult.get.lines(0).results shouldBe (correctRez_AggregationUniqueTotal)
  }
}