package test.processing

import slogger.services.processing.Calculator
import slogger.model.specification.Bundle
import com.github.nscala_time.time.Imports._
import slogger.model.specification.extraction.ExtractionSpecs
import slogger.model.specification.extraction.TimeLimits
import slogger.model.specification.extraction.SlicingSpecs
import slogger.model.common.TimePeriod
import slogger.model.specification.aggregation.AggregationSpecs
import slogger.services.processing.aggregation.aggregator.onefield.CountAggregator
import slogger.services.processing.aggregation.aggregator.onefield
import play.api.libs.json.Json
import play.api.libs.json.JsObject
import slogger.services.processing.extraction.DataExtractorImpl
import slogger.services.processing.extraction.DataExtractorDaoMongo
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator


class CalculatorTest extends BaseDaoTest {

  behavior of "Calculator"
  
  val dataTimeInterval = new Interval(
    new DateTime("2012-01-09T22:00:01.687Z"),
    new DateTime("2012-01-10T21:59:58.339Z")
  )
  
  
  
  val defaultCalcInterval = {
    //Test logs max and min dates
    //ISODate("2012-01-10T21:59:59.790Z")
    //ISODate("2012-01-09T22:00:00.027Z")
    
    val startDate = new DateTime(2012, 1, 10, 2, 0, 0, 0, DateTimeZone.UTC);
    val endDate = new DateTime(2012, 1, 10, 19, 0, 0, 0, DateTimeZone.UTC);
    new Interval(startDate, endDate);
  }
  
  
  ignore should "calculate" in {
       
    
    val calculator = Calculator.create(dbProvider)
    val specs = Bundle(
      extraction = ExtractionSpecs(
        filter = None,
        projection = None,
        timeLimits = TimeLimits(defaultCalcInterval),
        slicing = Some(SlicingSpecs(
          sliceDuration = TimePeriod.duration(TimePeriod.Hour),
          snapTo = defaultCalcInterval.end
        )),
        customCollectionName = Some("xlogs")
      ),
      aggregation = AggregationSpecs(
        aggregatorClass = classOf[CountAggregator].getName(),
        config = Json.toJson(onefield.Config("level")).as[JsObject]
      ) 
    )
    
    val rez = calculator.calculate(specs)
    println("============================\n" + rez.total.get + "\n--------------")
    println(rez.lines.size)
    
  }
  
  "Iteratee" should "work" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    
    val e = Enumerator.apply(1,2,3,4)
    val i = Iteratee.fold[Int,Int](0){ case (p1, p2) => 
      if (p1 == 3) throw new Exception("Ex--------")
      p1 + p2
    
    }
    
    try {val rez = twait(e.run(i))
    println("=2=======================================")
    println(rez)
    println("=1=======================================")
    } catch {
      case ex => ex.printStackTrace()
    }
  }
  

}