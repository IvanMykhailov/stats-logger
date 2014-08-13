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
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import slogger.model.processing.AggregationException
import slogger.model.processing.StatsError


trait Calculator {
  def calculate(specs: SpecsBundle): Future[CalculationResult]
  
}


class CalculatorImpl(
  extractor: DataExtractor,
  aggregatorResolver: AggregatorResolver,
  hystoryProvider: StatsResultProvider,
  executionContext: ExecutionContext
) extends Calculator {
  
  implicit val implicitExecutionContext = executionContext
  
  
  override def calculate(specs: SpecsBundle): Future[CalculationResult] = {
    val now = DateTime.now
    
    val calcFuture = calculateInt(specs, now)
    
    calcFuture.map { rez => 
      CalculationResult(  
        bundle = specs,
        calculatedAt = now,
        statsResult = Some(rez)
      )    
    }.recover {
      case aex: AggregationException =>
        CalculationResult(  
        bundle = specs,
        calculatedAt = now,
        statsError = Some(StatsError(aex.getMessage, aex.errorDocument))
      )
    }
  }
  
  
  protected def calculateInt(specs: SpecsBundle, now: DateTime): Future[StatsResult] = {    
    hystoryProvider.findByBundle(specs).flatMap { oldCalcOpt =>
      val oldSlicesResults = oldCalcOpt.map(_.lines).getOrElse(Seq.empty)      
      val reusableSlicesResults = oldSlicesResults.filter(_.slice.complete).map(c => (c.slice, c)).toMap
    
      val data = extractor.extract(specs.extraction, now)
      val aggregator: Aggregator = aggregatorResolver.resolve(specs.aggregation.aggregatorClass, specs.aggregation.config).get
      
      val aggregationFutures = data.map { case (slice, sliceData) =>
        reusableSlicesResults.get(slice) match {
          case Some(oldRez) => println("Reused: "+slice);Future.successful(oldRez)
          case None => aggregator.aggregate(slice, sliceData >>> Enumerator.enumInput(Input.EOF))
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