package test.processing

import slogger.services.processing.extraction.DbProvider
import slogger.services.processing.extraction.DirectMongoDbProvider
import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import org.scalatest.FlatSpec
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.Awaitable


abstract class BaseDaoTest extends FlatSpec with Matchers with PropertyChecks {

  val dbProvider: DbProvider = new DirectMongoDbProvider(dbName = "slogger_test", hosts = Seq("localhost"))
  
  init()  
  
  def init(): Unit = BaseDaoTest.synchronized {
    println("Start data initilizing ...")
    val collection: JSONCollection = dbProvider.db.collection("xlogs")
    val text = io.Source.fromInputStream(getClass.getResourceAsStream("/testLogs.json"))
    
    
    val empty = twait(collection.find(Json.obj()).cursor[JsObject].headOption).isEmpty
    val needDataLoad = if (empty) {
      true
    } else {
      val stats = twait(collection.stats)
      stats.count != 51928
    }
        
    if (needDataLoad) {
      //Optimization: load test data only if collection is empty
      println("Require data reloading")
      twait(collection.remove(Json.obj()))
      text.getLines.foreach { line => 
        val json = Json.parse(line).as[JsObject] - ("_id")
        twait(collection.save(json))
      }  
    }
    println("Complete data initilizing")
  } 
  
  def twait[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration(5, "minutes"))
}


object BaseDaoTest {
  
}