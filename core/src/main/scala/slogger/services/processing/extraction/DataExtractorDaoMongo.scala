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
  
  def load(times: Interval, filter: Option[JsObject], projection: Option[JsObject]): Enumerator[JsObject] = {
    
    val dateFilters = Seq(
      obj("time" -> obj("$gte" -> jsonDate(times.start))),
      obj("time" -> obj("$lte" -> jsonDate(times.end)))
    )
    
    val query = if (filter.isEmpty || filter.get.fields.isEmpty) {
      obj("$and" -> dateFilters)
    } else {
      obj("$and" -> (dateFilters :+ filter.get))
    }
    
    println(query)
    
    (projection match {
      case Some(p) => logCollection.find(query, p)
      case None => logCollection.find(query)
    }).cursor[JsObject].enumerate(stopOnError = false)
  }
  
  private def jsonDate(time: DateTime) = Json.obj("$date" -> time.getMillis())
}