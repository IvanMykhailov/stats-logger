package slogger.services.processing.history

import slogger.model.specification.SpecsBundle
import scala.concurrent.Future
import slogger.model.processing.StatsResult


trait StatsResultProvider {
  def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]]
}