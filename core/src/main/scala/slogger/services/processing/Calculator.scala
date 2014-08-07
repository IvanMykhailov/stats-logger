package slogger.services.processing

import slogger.model.specification.Bundle
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
import slogger.model.processing.SliceAggregated
import slogger.model.processing.Slice


trait Calculator {
  def calculate(specs: Bundle): StatsResult
  
}


class CalculatorImpl(
  extractor: DataExtractor,
  aggregatorResolver: AggregatorResolver,
  executionContext: ExecutionContext
) extends Calculator {
  
  override def calculate(specs: Bundle): StatsResult = {
    val now = DateTime.now
    
    val data = extractor.extract(specs.extraction, now)
    val aggregator: Aggregator = aggregatorResolver.resolve(specs.aggregation.aggregatorClass, specs.aggregation.config).get
    
    val aggregated = data.map { case (slice, sliceData) => 
      val future = aggregator.aggregate(slice, sliceData)(executionContext)
      //use await since we don't need simultaneous execution of mongo requests for all slices
      Await.result(future, scala.concurrent.duration.Duration(60, "minutes"))
    }
    
    val total = if (aggregator.isSliceMergingSupported) {
      Some(aggregator.mergeSlices(aggregated))
    } else {
      None
    }
    
    StatsResult(
      lines = onlyData(aggregated),
      total = total
    )
  }
  
  protected def onlyData(aggregated: Seq[SliceAggregated]): Seq[(Slice, Map[String, BigDecimal])] = 
    aggregated.map { a => (a.slice, a.results)}
}


object Calculator {
  def create(dbProvider: DbProvider): Calculator = {
    val dao = new DataExtractorDaoMongo(dbProvider)
    val extractor = new DataExtractorImpl(dao)
    val aggregatorResolver = new AggregatorResolverImpl
    new CalculatorImpl(
      extractor,
      aggregatorResolver,
      scala.concurrent.ExecutionContext.global
    )
  }  
}