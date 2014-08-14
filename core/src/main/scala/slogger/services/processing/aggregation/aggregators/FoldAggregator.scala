package slogger.services.processing.aggregation.aggregators

import slogger.services.processing.aggregation.Aggregator
import play.api.libs.json.JsObject
import slogger.model.processing.SliceResult
import slogger.model.processing.Slice
import slogger.utils.IterateeUtils
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator


trait FoldAggregator[S] extends Aggregator {
  
  protected def foldInitState: S
  
  protected def folder(state: S, json: JsObject): S
  
  protected def resultMapper(slice: Slice, rez: S): SliceResult 
  
  
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceResult] = {
    val rezF = dataEnumerator |>>| iteratee map(IterateeUtils.unwrapErrortoException)
    rezF.map { rez => resultMapper(slice, rez) }
  }
    
  protected def iteratee(implicit ec: ExecutionContext) = IterateeUtils.foldWithExceptionHandling(foldInitState)(folder)
}