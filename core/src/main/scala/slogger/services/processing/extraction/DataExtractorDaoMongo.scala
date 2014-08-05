package slogger.services.processing.extraction

import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import Json.{obj, arr}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Enumerator


class DataExtractorDaoMongo(
  dbProvider: DbProvider    
) extends DataExtractorDao {

  val logCollection: JSONCollection = dbProvider.db.collection("logs")
  
  def load(times: Interval, filter: JsObject, projection: JsObject): Enumerator[JsObject] = {
    val timesFilter = obj("$and" -> arr(
      obj("time" -> obj("$gte" -> Json.toJson(times.start))),
      obj("time" -> obj("$lte" -> Json.toJson(times.end)))
    ))
    
    val query = if (filter.fields.isEmpty) {
      timesFilter 
    } else {
      obj("$and" -> arr(
        filter,
        timesFilter
      ))
    }
    logCollection.find(query, projection).cursor[JsObject].enumerate(stopOnError = false)
  }
}