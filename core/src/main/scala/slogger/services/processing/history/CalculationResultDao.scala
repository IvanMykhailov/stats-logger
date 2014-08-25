package slogger.services.processing.history

import slogger.services.processing.extraction.DbProvider
import slogger.model.specification.CalculationSpecs
import scala.concurrent.Future
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import slogger.model.BsonHandlers._
import reactivemongo.bson.BSONDocument
import java.util.UUID
import slogger.model.processing.CalculationResult


trait CalculationResultDao {
  
  def findBySpecs(specs: CalculationSpecs): Future[Option[CalculationResult]] = {
    findById(specs.id).map { _.flatMap { rec =>
      if (rec.calculationSpecs.equalsIgnoreTime(specs)) { 
        Some(rec)
      } else {
        None
      }
    }}
  }

  def save(statsResult: CalculationResult): Future[Unit]
  
  def findById(id: String): Future[Option[CalculationResult]]
}


class CalculationResultDaoMongo(dbProvider: DbProvider) extends CalculationResultDao {
  
  val collection: BSONCollection = dbProvider.db.collection("slogger_calculationResults")  
      
  override def save(calcRez: CalculationResult): Future[Unit] = collection.save(calcRez).map(foo => Unit)  
  
  def findOne(query: BSONDocument): Future[Option[CalculationResult]] = collection.find(query).cursor[CalculationResult].headOption
  
  override def findById(entityId: String): Future[Option[CalculationResult]] = findOne(BSONDocument("_id" -> entityId))
  
}