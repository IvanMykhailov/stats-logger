package test.processing.calc

import slogger.model.specification.CalculationSpecs
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.model.common.TimePeriod
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.services.processing.aggregation.aggregators.onefield.SumAggregator
import play.api.libs.json._
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import org.joda.time.DateTime
import slogger.services.processing.aggregation.aggregators.onefield
import slogger.services.processing.CalculatorContext
import slogger.model.processing.SliceResult
import slogger.model.processing.BsonHandlers.StatsResultHandler
import slogger.model.processing.BsonHandlers.SliceResultHandler
import slogger.model.processing.StatsResult
import reactivemongo.bson.BSONDocument


class CalculationResultSavingTest extends BaseCalculationTest {

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
  
  "SliceResult" should "be serialized and deserialized" in {
    val rez = SliceResult(SumCalculationSlices(0)._1, SumCalculationSlices(0)._2)
    
    SliceResultHandler.write(rez)
    val deserialized = SliceResultHandler.read(SliceResultHandler.write(rez))    
    deserialized shouldBe rez
  }
  
  "StatsResult" should "be serialized and deserialized" in {
    val rez = StatsResult(
      lines = SumCalculationSlices.toSeq.map{case (slice, map) => SliceResult(slice, map)},
      total = None
    )    
    val deserialized = StatsResultHandler.read(StatsResultHandler.write(rez))    
    deserialized shouldBe rez
  }
  
  
  val context = new CalculatorContext(dbProvider)  
  val calculatorWithSaver = context.calculatorWithSaver  
  val rezDao = context.calculationResultDao
    
  
  "Calculator" should "calculate sum and save it" in {
    val specs = CalculationSpecs(
      extraction = extractionSpecs("characterLevel"),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[SumAggregator].getName(),
        config = Json.toJson(onefield.Config("characterLevel")).as[JsObject]
      )
    )
    
    val rez = twait(calculatorWithSaver.calculate(specs))
    rez.statsResult.get.total.get shouldBe (correctRez_AggregationSumTotal)
    
    val savedRez = twait(rezDao.findById(specs.id))
    savedRez shouldBe ('defined)
    savedRez.get.statsResult.get.total.get shouldBe (correctRez_AggregationSumTotal)    
    savedRez.get.statsResult.get.lines.map(_.results) shouldBe rez.statsResult.get.lines.map(_.results)
  }
}