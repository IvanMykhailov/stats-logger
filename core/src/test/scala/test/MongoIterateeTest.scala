package test

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import reactivemongo.api.MongoDriver
import reactivemongo.api.MongoConnection
import reactivemongo.api.DB
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext.global
import play.api.libs.iteratee.Iteratee
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.iteratee.Cont
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Error
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Step
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Input
import scala.util.control.NonFatal
import slogger.utils.IterateeUtils


class MongoIterateeTest extends FlatSpec with Matchers {
  val driver = new MongoDriver
  val connection = driver.connection(Seq("localhost"))
  val db = connection("iterateeTestCollection")(global)
 
  
  "IterateeUtils.wrapExceptionToError" should "work" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val it = Iteratee.foreach { i: Int => 
      if (i == 4) throw new Exception("!myException!")
      println(i)
    }
    val wit = IterateeUtils.wrapExceptionToError(it)    
    val rez = Enumerator(1,2,3,4,5).run(wit)
    intercept[RuntimeException] {
      Await.result(rez, Duration(5, "seconds"))
    }
  }
  
  
  /*
  "Enumerator" should "handle exceptions correctly" in {
    val collection: BSONCollection = db.collection("itest")
    Await.result(collection.insert(BSONDocument("test" -> 2)), Duration(5, "seconds"))
    
    val iteratee1 = Cont.apply[BSONDocument, Int] { i => Error("error--------------------", i) }
    
    val iteratee = Iteratee.fold(0) { (state, json: BSONDocument) => 
      throw new Exception()
    }
    val iterateeTest = collection.find(BSONDocument()).cursor.enumerate().apply(iteratee1)
    
    val tt =  Await.result(iterateeTest, Duration(5, "seconds")).unflatten
    
    println("==="+Await.result(tt, Duration(5, "seconds")))   
  }
  */
}
