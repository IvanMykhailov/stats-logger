package slogger.services.helpers.recalculation

import scala.concurrent.Future

trait RecalculableSpecsDao {  
  def listAllRecalculableSpecs(): Future[Seq[RecalculableSpecs]]
}