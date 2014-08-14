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


class IterateeUtilsTest extends FlatSpec with Matchers {
  
  "IterateeUtils.foldWithExceptionHandling" should "work" in {
    import scala.concurrent.ExecutionContext.Implicits.global    
    
    val it = IterateeUtils.foldWithExceptionHandling[Int,Int](0) { case (i1, i2) =>
      if (i2 == 3) throw new Exception("!myException!")
      i1 + i2
    }
    
    val rez = Enumerator(1,2,3,4,5) |>>| it
        
    val step = Await.result(rez, Duration(5, "seconds"))
    step shouldBe (Step.Error("!myException!", Input.El(3)))
  }
}
