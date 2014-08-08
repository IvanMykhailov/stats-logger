package slogger.services.processing.extraction

import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import Json.{obj, arr}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Enumerator
import play.modules.reactivemongo.json.collection.JSONCollection


class DataExtractorDaoMongo(
  dbProvider: DbProvider    
) extends DataExtractorDao {
  
  val defaultLogCollection: JSONCollection = dbProvider.db.collection("logs")
  
  override def load(times: Interval, filter: Option[JsObject], projection: Option[JsObject], customCollectionName: Option[String] = None): Enumerator[JsObject] = {
    val collection = customCollectionName.map( collectionName => dbProvider.db.collection[JSONCollection](collectionName)).getOrElse(defaultLogCollection)
    
    val dateFilters = Seq(
      obj("time" -> obj("$gte" -> jsonDate(times.start))),
      obj("time" -> obj("$lt" -> jsonDate(times.end)))
    )
    
    val query = if (filter.isEmpty || filter.get.fields.isEmpty) {
      obj("$and" -> dateFilters)
    } else {
      obj("$and" -> (dateFilters :+ filter.get))
    }
        
    (projection match {
      case Some(p) => collection.find(query, p)
      case None => collection.find(query)
    }).cursor[JsObject].enumerate(stopOnError = false)
  }
  
  private def jsonDate(time: DateTime) = Json.obj("$date" -> time.getMillis())
}