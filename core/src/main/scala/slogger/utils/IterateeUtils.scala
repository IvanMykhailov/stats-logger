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


object IterateeUtils {
  
  def wrapExceptionToError[E, A](it: Iteratee[E, A]): Iteratee[E, A] = {
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
  }
  
}