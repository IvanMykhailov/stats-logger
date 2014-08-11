package slogger.services.processing.history

import slogger.services.processing.extraction.DbProvider
import slogger.model.specification.SpecsBundle
import slogger.model.processing.StatsResult
import scala.concurrent.Future
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import slogger.model.BsonHandlers._
import reactivemongo.bson.BSONDocument
import java.util.UUID


trait StatsResultDao {
  
  def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]]

  def save(statsResult: StatsResult): Future[Unit]
}


class StatsResultDaoMongo(dbProvider: DbProvider) extends StatsResultDao {
  
  val statsResultsCollection: BSONCollection = dbProvider.db.collection("statsResults")  
  
    
  def save(statsResult: StatsResult): Future[Unit] = statsResultsCollection.save(statsResult).map(foo => Unit)
  
  
  def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]] = {
    findById(specs.id).map { _.flatMap { rec =>
      println(rec.bundle.get)
      println(specs)
      if (rec.bundle.isDefined && rec.bundle.get.equalsIgnoreTime(specs)) { 
        Some(rec)
      } else {
        None
      }
    }}
  }
  
  def findOne(query: BSONDocument): Future[Option[StatsResult]] = statsResultsCollection.find(query).cursor[StatsResult].headOption
  
  def findById(entityId: UUID): Future[Option[StatsResult]] = findOne(BSONDocument("_id" -> entityId))
  
}

