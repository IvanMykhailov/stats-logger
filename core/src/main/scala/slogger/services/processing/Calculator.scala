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
import slogger.services.processing.history.StatsResultDaoMongo
import slogger.services.processing.history.StatsResultDao
import slogger.services.processing.history.StatsResultProvider


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
          case Some(oldRez) => Future.successful(oldRez)
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
          total = total,
          calcTime = now,
          bundle = Some(specs)
        )
      }
    }
  }
  
}


object Calculator {
  def create(dbProvider: DbProvider): Calculator = {
    val dao = new DataExtractorDaoMongo(dbProvider)
    val statsResultsDao = new StatsResultDaoMongo(dbProvider)
    val extractor = new DataExtractorImpl(dao)
    val aggregatorResolver = new AggregatorResolverImpl
    
    new CalculatorImpl(
      extractor,
      aggregatorResolver,
      statsResultsDao,
      scala.concurrent.ExecutionContext.global
    )
  }  
}