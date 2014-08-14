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
import slogger.model.processing.CalculationMetaStats
import java.util.concurrent.atomic.AtomicLong
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.JsObject


trait Calculator {
  def calculate(specs: SpecsBundle): Future[CalculationResult]
  
}


class CalculatorImpl(
  extractor: DataExtractor,
  aggregatorResolver: AggregatorResolver,
  hystoryProvider: StatsResultProvider,
  executionContext: ExecutionContext
) extends Calculator {
  import CalculatorImpl._
  
  implicit val implicitExecutionContext = executionContext
  
  
  override def calculate(specs: SpecsBundle): Future[CalculationResult] = {
    val startTime = DateTime.now
    
    val calcFuture = calculateInt(specs, startTime)
    
    calcFuture.map { rez =>
      CalculationResult(  
        bundle = specs,
        calculatedAt = startTime,
        metaStats = CalculationMetaStats(
          processedDocuments = rez._1.documents,
          reusedSlices = rez._1.reusedSlices,
          processingTime = new Duration(startTime, DateTime.now)
        ),
        statsResult = Some(rez._2)
      )    
    }.recover {
      case aex: AggregationException =>
        CalculationResult(  
        bundle = specs,
        calculatedAt = startTime,
        metaStats = CalculationMetaStats(
          processedDocuments = -1,
          reusedSlices = -1,
          processingTime = new Duration(startTime, DateTime.now)
        ),
        statsError = Some(StatsError(aex.getMessage, aex.errorDocument))
      )
    }
  }
  

  protected def calculateInt(specs: SpecsBundle, now: DateTime): Future[(IntMetaStats, StatsResult)] = {    
    hystoryProvider.findByBundle(specs).flatMap { oldCalcOpt =>
      val oldSlicesResults = oldCalcOpt.map(_.lines).getOrElse(Seq.empty)      
      val reusableSlicesResults = oldSlicesResults.filter(_.slice.complete).map(c => (c.slice, c)).toMap
    
      val data = extractor.extract(specs.extraction, now)
      val aggregator: Aggregator = aggregatorResolver.resolve(specs.aggregation.aggregatorClass, specs.aggregation.config).get
      
      //val reusedSlicesCounter = new AtomicLong
      //val (documentCounter, docCounterEnumeratee) = counterEnumeratee
      
      val aggregationFutures = data.map { case (slice, sliceDataEnumerator) =>
        reusableSlicesResults.get(slice) match {
          case Some(oldRez) => 
            //reusedSlicesCounter.incrementAndGet()
            Future.successful(oldRez)
          case None =>
            aggregator.aggregate(slice, (sliceDataEnumerator/* &> docCounterEnumeratee*/) >>> Enumerator.enumInput(Input.EOF))
        }
      }
      
      Future.sequence(aggregationFutures).map { slicesResults =>
        val total = if (aggregator.isSliceMergingSupported) {
          Some(aggregator.mergeSlices(slicesResults))
        } else {
          None
        }
        
        val metaStats = IntMetaStats(
          documents = 1,//documentCounter.get(), 
          reusedSlices = 1//reusedSlicesCounter.get()
        )
        
        val rez = StatsResult(
          lines = slicesResults,
          total = total
        )
        
        (metaStats, rez)
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


object CalculatorImpl {
  protected case class IntMetaStats(
    documents: Long,
    reusedSlices: Long
  ) 
  protected def counterEnumeratee(implicit ec: ExecutionContext): (AtomicLong, Enumeratee[JsObject, JsObject]) = {
    val counter = new AtomicLong()
    val enumeratee = Enumeratee.map[JsObject]({e => /*counter.incrementAndGet();*/ e})
    (counter, enumeratee)
  } 
}