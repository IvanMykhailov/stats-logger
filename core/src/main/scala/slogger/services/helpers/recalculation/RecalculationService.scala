package slogger.services.helpers.recalculation

import slogger.services.processing.history.CalculationResultDao
import scala.concurrent.Await
import com.github.nscala_time.time.Imports._
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue
import slogger.services.processing.Calculator
import java.util.concurrent.ScheduledFuture
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory


//Periodically recalculate all required Specs 
trait RecalculationService {
  
  def start(): Unit
  
  def stop(): Unit
  
}


class RecalculationServiceImpl(
  calcRezDao: CalculationResultDao,
  processingSpecsProvider: RecalculableSpecsDao,
  calculator: Calculator  
) extends RecalculationService {
  val log = LoggerFactory.getLogger("slogger")
  
  val timeout = scala.concurrent.duration.Duration(1, "minute")
  
  val recalculationQueue = new LinkedBlockingQueue[RecalculableSpecs]
  
  val executor = new ScheduledThreadPoolExecutor(2)
  
  var jobs = Seq[ScheduledFuture[_]]()
  
  
  override def start(): Unit = this.synchronized {
    val recheckJob = executor.scheduleAtFixedRate(
      new Runnable { def run() { recheck }}, 
      0, //initial delay
      20, TimeUnit.SECONDS
    )   
      
    val recalculateJob = executor.schedule(
      new Runnable { def run() { 
        while(!Thread.interrupted()){
          val specs = recalculationQueue.take()
          try {            
            log.info("Start recalculation: " + specs.name)
            val f = calculator.calculate(specs.calculationSpecs, calculateToDate())
            Await.ready(f, scala.concurrent.duration.Duration(30, TimeUnit.HOURS))
            f.onFailure { case NonFatal(ex) => log.error(s"Calc[id=${specs.id}]: calculation error, $ex", ex)}
            log.info("Done recalculation: " + specs.name)
          } catch {
            case NonFatal(ex) => log.error(s"Calc[id=${specs.id}]: calculation error, $ex")
          }
        }
      }},
      0, TimeUnit.MINUTES //run immediately
    )
    
    jobs = Seq(recheckJob, recalculateJob)
  }
  
  
  override def stop(): Unit = this.synchronized {
    jobs.foreach(_.cancel(true))
    jobs = Seq()
  }
  
  
  def recheck(): Unit = {
    val calcRequired = isRecalcRequired(calculateToDate()) _
    val specsToRecalcFuture = processingSpecsProvider.listAllRecalculableSpecs.map(_.filter(calcRequired))
    
    specsToRecalcFuture.map { specsToRecalc =>
      log.info("Need recalculation: " + specsToRecalc.map(_.name).mkString(", "))    
      specsToRecalc.foreach { specs => 
        if (!recalculationQueue.contains(specs)) {
          recalculationQueue.add(specs)
        }
      }
    }
  }
  
  
  def isRecalcRequired(now: DateTime)(specs: RecalculableSpecs): Boolean = {
    val calcRezOpt = Await.result(calcRezDao.findById(specs.id), timeout)    
    calcRezOpt match {
      case Some(calcRez) => 
        val isOutdated = calcRez.calculatedAt < now - specs.recalculationTime
        val isSpecsChanged = (calcRez.calculationSpecs != specs.calculationSpecs)
        isOutdated || isSpecsChanged 
      case None => true 
    }
  }
  
  
  def calculateToDate(): DateTime = DateTime.now
}