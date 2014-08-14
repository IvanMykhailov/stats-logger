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
import play.api.libs.iteratee.IterateeExeption
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

  "Wrapping exception to error" should "work" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val it = Iteratee.fold[Int,Int](0) { case (i1, i2) =>
      if (i2 == 3) throw new Exception("!myException!")
      i1 + i2
    }

    import play.api.libs.iteratee.Execution.Implicits.{ defaultExecutionContext => dec }

    implicit class IterateeWithCoolStuff[E, +A](iteratee: Iteratee[E, A]) {
      def inputPreservingRecoverWith[B >: A](pf: Input[E] => PartialFunction[Throwable, Iteratee[E, B]])(implicit ec: ExecutionContext): Iteratee[E, B] = {
        val pec = ec.prepare()

        def step(it: Iteratee[E, A])(input: Input[E]): Iteratee[E, B] = {
          val nextIt = it.pureFlatFold[E, B] {
            case Step.Cont(k) =>
              val n = k(input)
              n.pureFlatFold {
                case Step.Cont(_) => Cont(step(n))
                case Step.Error(msg, _) => throw new IterateeExeption(msg)
                case other => other.it
              }(dec)
            case Step.Error(msg, _) => throw new IterateeExeption(msg)
            case other => other.it
          }(dec)

          Iteratee.flatten(
            nextIt.unflatten
              .map(_.it)(dec)
              .recover(pf(input))(pec)
          )
        }

        Cont(step(iteratee))
      }
    }

    val wrappedIt = it inputPreservingRecoverWith { input => {
      case x: Exception => new Iteratee[Int, Int] {
        override def fold[B](folder: (Step[Int, Int]) => Future[B])(implicit ec: ExecutionContext): Future[B] = {
          folder(Step.Error(x.getMessage, input))
        }
      }
    }}

    val rez = Enumerator(1,2,3,4,5) |>>| wrappedIt

    val step = Await.result(rez, Duration(5, "seconds"))
    step shouldBe Step.Error("!myException!", Input.El(3))
  }

}
