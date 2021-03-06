package slogger.utils

import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.Step
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Error
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.control.NonFatal
import play.api.libs.iteratee.Input
import scala.concurrent.duration.Duration
import play.api.libs.json.JsObject
import slogger.model.processing.AggregationException
import scala.util.{Try, Success, Failure}
import play.api.libs.iteratee.Cont
import play.api.libs.iteratee.Done


object IterateeUtils {
  
  
  def foldWithExceptionHandling[E, A](state: A)(f: (A, E) => A): Iteratee[E, A] = {
    def step(s: A)(i: Input[E]): Iteratee[E, A] = i match {
      case Input.EOF => 
        Done(s, Input.EOF)
      case Input.Empty => 
        Cont[E, A](step(s))
      case Input.El(e) => 
        try {
          val a = f(s, e); 
          Cont[E, A](step(a))
        } catch {
          case NonFatal(ex) => Error(ex.getMessage(), i)
        }   
    }
    (Cont[E, A](step(state)))
   }
  
  
  /*def wrapExceptionToError[E, A](it: Iteratee[E, A]): Iteratee[E, A] = {
    new Iteratee[E, A] {
      def fold[B](folder: Step[E, A] => Future[B])(implicit ec: ExecutionContext): Future[B] = {
        def newFolder(step: Step[E, A]): Future[B] = {
          val newStep = step match {
            case Step.Cont(k) => Step.Cont { (i: Input[E]) =>
              try {
                val it = k(i)
                //force calculation in this thread to catch exception
                Await.result(it.unflatten, Duration(1, "minute"))
                wrapExceptionToError(it)
              } catch {
                case NonFatal(ex) => Error(ex.getMessage(), i)
              }
            }
            case step => step
          }
          folder(newStep)
        }
        it.fold(newFolder)(scala.concurrent.ExecutionContext.Implicits.global)
      }
    }
  }*/
  
  
  def unwrapErrortoException[A](step: Step[JsObject, A]): A = step match {
    case Step.Done(rez, remaining) => rez
      case Step.Error(msg, input) => 
        val errorInput = input match { 
          case Input.El(e) => Some(e)
          case _ => None
        }        
        throw new AggregationException(msg, errorInput)
      case _: Step.Cont[_,_] => throw new AggregationException("Future is in Cont step even when enumerator is finished") 
  }
}