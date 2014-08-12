package slogger.services.processing

import slogger.model.specification.SpecsBundle
import slogger.model.processing.StatsResult
import slogger.services.processing.extraction.DataExtractor
import slogger.services.processing.aggregation.AggregatorResolver
import com.github.nscala_time.time.Imports._
import slogger.services.processing.aggregation.Aggregator
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import slogger.services.processing.extraction.DbProvider
import slogger.services.processing.extraction.DataExtractorDaoMongo
import slogger.services.processing.extraction.DataExtractorImpl
import slogger.services.processing.aggregation.AggregatorResolverImpl
import slogger.model.processing.SliceResult
import slogger.model.processing.Slice
import scala.concurrent.Future
import slogger.services.processing.history.StatsResultProvider
import slogger.services.processing.extraction.DataExtractorDao
import slogger.model.processing.CalculationResult
import slogger.services.processing.history.CalculationResultDaoMongo
import slogger.services.processing.history.CalculationResultDao
import slogger.services.processing.history.StatsResultProviderByDao


trait Calculator {
  def calculate(specs: SpecsBundle): Future[StatsResult]
  
}


class CalculatorImpl(
  extractor: DataExtractor,
  aggregatorResolver: AggregatorResolver,
  hystoryProvider: StatsResultProvider,
  executionContext: ExecutionContext
) extends Calculator {
  
  implicit val implicitExecutionContext = executionContext
  
  override def calculate(specs: SpecsBundle): Future[StatsResult] = {
    val now = DateTime.now
    
    hystoryProvider.findByBundle(specs).flatMap { oldCalcOpt =>
      val oldSlicesResults = oldCalcOpt.map(_.lines).getOrElse(Seq.empty)      
      val reusableSlicesResults = oldSlicesResults.filter(_.slice.complete).map(c => (c.slice, c)).toMap
    
      val data = extractor.extract(specs.extraction, now)
      val aggregator: Aggregator = aggregatorResolver.resolve(specs.aggregation.aggregatorClass, specs.aggregation.config).get
      
      val aggregationFutures = data.map { case (slice, sliceData) =>
        reusableSlicesResults.get(slice) match {
          case Some(oldRez) => println("Reused: "+slice);Future.successful(oldRez)
          case None => aggregator.aggregate(slice, sliceData)
        }
      }
      
      Future.sequence(aggregationFutures).map { slicesResults =>
        val total = if (aggregator.isSliceMergingSupported) {
          Some(aggregator.mergeSlices(slicesResults))
        } else {
          None
        }   
        
        StatsResult(
          lines = slicesResults,
          total = total
        )
      }
    }
  }
  
}


class CalculatorContext(dbProvider: DbProvider) {
  lazy val extractionDao: DataExtractorDao = new DataExtractorDaoMongo(dbProvider) 
  lazy val calculationResultDao: CalculationResultDao = new CalculationResultDaoMongo(dbProvider)
  lazy val statsResultProvider: StatsResultProvider = new StatsResultProviderByDao(calculationResultDao)
  lazy val extractor: DataExtractor = new DataExtractorImpl(extractionDao)
  lazy val aggregatorResolver: AggregatorResolver = new AggregatorResolverImpl
  lazy val calculator: Calculator = new CalculatorImpl(
    extractor,
    aggregatorResolver,
    statsResultProvider,
    scala.concurrent.ExecutionContext.global
  ) 
}


object Calculator {
  def create(dbProvider: DbProvider): Calculator = {
    val context = new CalculatorContext(dbProvider)
    context.calculator
  }  
}