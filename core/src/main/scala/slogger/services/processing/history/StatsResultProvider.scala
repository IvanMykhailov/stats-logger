package slogger.services.processing.history

import slogger.model.specification.SpecsBundle
import scala.concurrent.Future
import slogger.model.processing.StatsResult
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID


trait StatsResultProvider {
  def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]]
}


class StatsResultProviderByDao(dao: CalculationResultDao) extends StatsResultProvider {
  
  override def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]] = dao.findByBundle(specs).map(_.flatMap(_.statsResult))
}


class StatsResultProviderStub(bundle: SpecsBundle, statsResult: StatsResult) extends StatsResultProvider {
  
  override def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]] = {
    Future.successful{
      if (specs.equalsIgnoreTime(bundle)) 
        Some(statsResult)
      else 
        None
    }
  } 
    
}