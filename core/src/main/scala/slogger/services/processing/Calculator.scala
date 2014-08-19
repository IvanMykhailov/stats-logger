package slogger.services.processing

import slogger.model.specification.CalculationSpecs
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
import org.slf4j.LoggerFactory
import scala.util.control.NonFatal


trait Calculator {
  def calculate(specs: CalculationSpecs): Future[CalculationResult]  
}


class CalculatorImpl(
  extractor: DataExtractor,
  aggregatorResolver: AggregatorResolver,
  hystoryProvider: StatsResultProvider,
  executionContext: ExecutionContext
) extends Calculator {
  import CalculatorImpl._
  
  val log = LoggerFactory.getLogger("sloger")
  
  implicit val implicitExecutionContext = executionContext
  
  
  override def calculate(specs: CalculationSpecs): Future[CalculationResult] = {
    val startTime = DateTime.now
    log.debug(s"Calc[id=${specs.id}]: start calculation")
    
    val calcFuture = calculateInt(specs, startTime)
    
    calcFuture.map { rez =>
      val calculationTime = new Duration(startTime, DateTime.now)
      log.debug(s"Calc[id=${specs.id}]: calculation took $calculationTime time")
      CalculationResult(  
        calculationSpecs = specs,
        calculatedAt = startTime,
        metaStats = CalculationMetaStats(
          processedDocuments = rez._1.documents,
          reusedSlices = rez._1.reusedSlices,
          processingTime = calculationTime
        ),
        statsResult = Some(rez._2)
      )    
    }.recover {
      case aex: AggregationException =>
        log.error(s"Calc[id=${specs.id}]: error, ${aex}")
        CalculationResult(  
          calculationSpecs = specs,
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
  

  protected def calculateInt(specs: CalculationSpecs, now: DateTime): Future[(IntMetaStats, StatsResult)] = {    
    hystoryProvider.findBySpecs(specs).flatMap { oldCalcOpt => 
      log.debug(s"Calc[id=${specs.id}]: reuse old calculation - ${oldCalcOpt.isDefined}")
      
      val oldSlicesResults = oldCalcOpt.map(_.lines).getOrElse(Seq.empty)      
      val reusableSlicesResults = oldSlicesResults.filter(_.slice.complete).map(c => (c.slice, c)).toMap
    
      val data = extractor.extract(specs.extraction, now)
      val aggregator: Aggregator = aggregatorResolver.resolve(specs.aggregation.aggregatorClass, specs.aggregation.config).get
      
      val reusedSlicesCounter = new AtomicLong
      val (documentCounter, docCounterEnumeratee) = counterEnumeratee
      
      val aggregationFutures = data.map { case (slice, sliceDataEnumerator) =>
        reusableSlicesResults.get(slice) match {
          case Some(oldRez) => 
            reusedSlicesCounter.incrementAndGet()
            Future.successful(oldRez)
          case None =>
            aggregator.aggregate(slice, (sliceDataEnumerator &> docCounterEnumeratee) >>> Enumerator.enumInput(Input.EOF))
        }
      }
      
      Future.sequence(aggregationFutures).map { slicesResults =>
        val total = if (aggregator.isSliceMergingSupported) {
          Some(aggregator.mergeSlices(slicesResults))
        } else {
          None
        }
        
        val metaStats = IntMetaStats(
          documents = documentCounter.get(), 
          reusedSlices = reusedSlicesCounter.get()
        )
        
        log.debug(s"Calc[id=${specs.id}]: ${slicesResults.length} slices")
        log.debug(s"Calc[id=${specs.id}]: ${metaStats.reusedSlices} slices reused")
        log.debug(s"Calc[id=${specs.id}]: ${metaStats.documents} documents processed")
        
        val rez = StatsResult(
          lines = slicesResults,
          total = total
        )
        
        (metaStats, rez)
      }
    }
  }
  
}


class HistorySavingCalculator(
  baseCalculator: Calculator,
  calculationResultDao: CalculationResultDao
) extends Calculator {
  
  val log =  LoggerFactory.getLogger("Calculator")
  
  override def calculate(specs: CalculationSpecs): Future[CalculationResult] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    baseCalculator.calculate(specs) 
      .flatMap(rez => calculationResultDao.save(rez).map(any => rez))
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
  lazy val calculatorWithSaver = new HistorySavingCalculator(calculator, calculationResultDao)
}


object CalculatorImpl {
  protected case class IntMetaStats(
    documents: Long,
    reusedSlices: Long
  ) 
  protected def counterEnumeratee(implicit ec: ExecutionContext): (AtomicLong, Enumeratee[JsObject, JsObject]) = {
    val counter = new AtomicLong()
    val enumeratee = Enumeratee.map[JsObject]({e => counter.incrementAndGet(); e})
    (counter, enumeratee)
  } 
}