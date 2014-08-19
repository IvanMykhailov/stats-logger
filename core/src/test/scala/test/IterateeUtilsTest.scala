package test

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Enumerator
import slogger.utils.IterateeUtils
import play.api.libs.iteratee.Step
import play.api.libs.iteratee.Input


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
  
  /*
  "Wrapping exception to error" should "work" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    
    
    val it = Iteratee.fold[Int,Int](0) { case (i1, i2) =>
      if (i2 == 3) throw new Exception("!myException!")
      i1 + i2
    }
    
    val wrappedIt = it//TODO: real wrapper
    
    val rez = Enumerator(1,2,3,4,5) |>>| wrappedIt
        
    val step = Await.result(rez, Duration(5, "seconds"))
    step shouldBe (Step.Error("!myException!", Input.El(3)))
  }*/
}
