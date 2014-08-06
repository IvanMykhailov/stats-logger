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

  val dbProvider: DbProvider = new DirectMongoDbProvider(dbName = "sloger_test", hosts = Seq("localhost"))
  
  init()
  
  
  def init(): Unit = {
    println("Start data initilizing ...")
    val collection: JSONCollection = dbProvider.db.collection("logs")
    val text = io.Source.fromInputStream(getClass.getResourceAsStream("/testLogs.json"))
    
    
    twait(collection.remove(Json.obj()))
    text.getLines.foreach { line => 
      val json = Json.parse(line)
      twait(collection.save(json))
    }
    println("Complete data initilizing")
  } 
  
  def twait[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration(5, "minutes"))
}