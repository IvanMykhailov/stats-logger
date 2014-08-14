package slogger.services.processing.history

import slogger.model.specification.CalculationSpecs
import scala.concurrent.Future
import slogger.model.processing.StatsResult
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID


trait StatsResultProvider {
  def findBySpecs(specs: CalculationSpecs): Future[Option[StatsResult]]
}


class StatsResultProviderByDao(dao: CalculationResultDao) extends StatsResultProvider {
  
  override def findBySpecs(specs: CalculationSpecs): Future[Option[StatsResult]] = dao.findBySpecs(specs).map(_.flatMap(_.statsResult))
}


class StatsResultProviderStub(specs: CalculationSpecs, statsResult: StatsResult) extends StatsResultProvider {
  
  override def findBySpecs(specs: CalculationSpecs): Future[Option[StatsResult]] = {
    Future.successful{
      if (specs.equalsIgnoreTime(specs)) 
        Some(statsResult)
      else 
        None
    }
  } 
    
}