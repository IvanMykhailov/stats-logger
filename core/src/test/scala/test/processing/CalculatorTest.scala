package test.processing

import slogger.services.processing.Calculator
import slogger.model.specification.Bundle
import com.github.nscala_time.time.Imports._
import slogger.model.specification.extraction.DataExtraction
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.Slicing
import slogger.model.common.TimePeriod
import slogger.model.specification.aggregation.Aggregation
import slogger.services.processing.aggregation.aggregator.onefield.CountAggregator
import slogger.services.processing.aggregation.aggregator.onefield
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import slogger.services.processing.extraction.DataExtractorImpl
import slogger.services.processing.extraction.DataExtractorDaoMongo
import play.api.libs.iteratee.Iteratee


class CalculatorTest extends BaseDaoTest {

  val dataTimeInterval = new Interval(
    new DateTime("2012-01-09T22:00:01.687Z"),
    new DateTime("2012-01-10T21:59:58.339Z")
  )
  
  "Calculator" should "calculate" in {
       
    
    val calculator = Calculator.create(dbProvider)
    val specs = Bundle(
      extraction = DataExtraction(
        filter = None,
        projection = None,
        timeLimits = TimeLimits(dataTimeInterval),
        slicing = Some(Slicing(
          sliceDuration = TimePeriod.duration(TimePeriod.Hour)
        )),
        customCollectionName = Some("xlogs")
      ),
      aggregation = Aggregation(
        aggregatorClass = classOf[CountAggregator].getName(),
        config = Json.toJson(onefield.Config("subclass")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    println("============================\n" + rez.total.get + "\n--------------")
    println(rez.lines.size)
    
    
    /*
    val dao = new DataExtractorDaoMongo(dbProvider)
    val extractor = new DataExtractorImpl(dao)
    
    extractor.extract(specs.extraction).foreach { case (slice, data) =>
      val d = twait(data.run(Iteratee.getChunks))
      println(slice + "   " + d.length)
    }*/
  }
  

}