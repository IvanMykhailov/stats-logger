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


trait StatsResultDao extends StatsResultProvider {
  
  def findByBundle(specs: SpecsBundle): Future[Option[StatsResult]] = {
    findByBundleId(specs.id).map { _.flatMap { rec =>
      if (rec.bundle.isDefined && rec.bundle.get.equalsIgnoreTime(specs)) { 
        Some(rec)
      } else {
        None
      }
    }}
  }

  def save(statsResult: StatsResult): Future[Unit]
  
  protected def findByBundleId(id: UUID): Future[Option[StatsResult]]
}


class StatsResultDaoMongo(dbProvider: DbProvider) extends StatsResultDao {
  
  val statsResultsCollection: BSONCollection = dbProvider.db.collection("statsResults")  
  
  override protected def findByBundleId(id: UUID): Future[Option[StatsResult]] = findById(id)
    
  override def save(statsResult: StatsResult): Future[Unit] = statsResultsCollection.save(statsResult).map(foo => Unit)  
  
  def findOne(query: BSONDocument): Future[Option[StatsResult]] = statsResultsCollection.find(query).cursor[StatsResult].headOption
  
  def findById(entityId: UUID): Future[Option[StatsResult]] = findOne(BSONDocument("_id" -> entityId))
  
}


class StatsResultDaoInmemory extends StatsResultDao {
  
  var map: Map[UUID, StatsResult] = Map.empty
  
  override def save(statsResult: StatsResult): Future[Unit] = this.synchronized {
    map = map + (statsResult.bundle.get.id -> statsResult)
    Future.successful(Unit)
  }
  
  override protected def findByBundleId(id: UUID): Future[Option[StatsResult]] = this.synchronized {
    Future.successful(map.get(id))
  }
}
