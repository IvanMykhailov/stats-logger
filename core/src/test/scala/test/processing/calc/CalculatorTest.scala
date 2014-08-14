package test.processing.calc

import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.ExtractionSpecs
import play.api.libs.json.Json
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import org.joda.time.DateTime
import slogger.model.specification.SpecsBundle
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.Aggregator
import scala.concurrent.ExecutionContext
import slogger.model.processing.Slice
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator
import slogger.model.processing.SliceResult
import play.api.libs.json.JsObject
import slogger.utils.IterateeUtils
import slogger.services.processing.aggregation.aggregators.onefield.SumAggregator
import slogger.services.processing.aggregation.aggregators.onefield.Config
import com.github.nscala_time.time.Imports._
import slogger.services.processing.aggregation.aggregators.onefield.AverageAggregator


class CalculatorTest extends BaseCalculationTest {
  
  val extraction = ExtractionSpecs(
    filter = None,
    projection = Some(Json.obj("characterLevel" -> 1)),
    timeLimits = TimeLimits(referenceCalcInterval),
    slicing = Some(SlicingSpecs(
      sliceDuration = TimePeriod.duration(TimePeriod.Day),
      snapTo = new DateTime(1493L)
    )),
    customCollectionName = Some("xlogs")
  )
  
  "Calculator" should "handle erros" in {
    val specs = SpecsBundle(
      extraction,
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[BrokenAggregator].getName(),
        config = Json.obj()
      ) 
    )
    
    val rez = twait(calculator.calculate(specs))
    
    rez should be ('isError)
    rez.statsError.get.message shouldBe ("ErrorShouldBeHandled")    
  }
  
  
  it should "provide statistic" in {
    val specs = SpecsBundle(
      extraction,
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[AverageAggregator].getName(),
        config = Json.toJson(Config("characterLevel")).as[JsObject]
      ) 
    ) 
    
    val rez = twait(calculator.calculate(specs))
    
    rez.metaStats.processingTime should be > new Duration(0)
    rez.metaStats.reusedSlices shouldBe 0
    rez.metaStats.processedDocuments shouldBe 35730
  }
}


class BrokenAggregator(config: JsObject) extends Aggregator {
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceResult] = {
    val iteratee = IterateeUtils.foldWithExceptionHandling(BigDecimal(0)) { (state: BigDecimal, json: JsObject) => 
      throw new IllegalArgumentException("ErrorShouldBeHandled")
    }
    
    val rezF = dataEnumerator |>>| iteratee map(IterateeUtils.unwrapErrortoException)
    
    rezF.map { rez =>  
      SliceResult(
        slice,
        Map("foobar" -> rez)
      )
    }
  }
  
}