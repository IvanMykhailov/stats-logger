package test.processing.calc

import slogger.model.specification.Bundle
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.common.TimePeriod
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregator.onefield.CountAggregator
import play.api.libs.json._
import slogger.services.processing.aggregation.aggregator.onefield


class CalculationPlainTest extends BaseCalculationTest {

  "Calculator" should "calculate counts" in {
    
    val specs = Bundle(
      extraction = ExtractionSpecs(
        filter = None,
        projection = None,
        timeLimits = TimeLimits(referenceCalcInterval),
        slicing = Some(SlicingSpecs(
          sliceDuration = TimePeriod.duration(TimePeriod.Hour)
        )),
        customCollectionName = Some("xlogs")
      ),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    rez.total.get shouldBe (correctRez_AggregationCountTotal)
  }
  
}