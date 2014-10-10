package test.processing

import org.scalatest.Matchers
import org.scalatest.prop.PropertyChecks
import org.scalatest.FlatSpec
import slogger.services.processing.aggregation.Aggregator
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator
import slogger.services.processing.aggregation.AggregatorResolverImpl
import play.api.libs.json._
import scala.util.Success
import scala.util.Try
import scala.util.Failure
import slogger.model.processing.SliceResult
import slogger.model.processing.Slice


class CorrectAggregator(config: JsObject) extends Aggregator {
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceResult] = null
}

class AggregatorWithIncorrectConstructor(config: JsObject, foo: Int) extends Aggregator {
  override def aggregate(slice: Slice, dataEnumerator: Enumerator[JsObject])(implicit ec: ExecutionContext): Future[SliceResult] = null
}


class AggregatorResolverTest extends FlatSpec with Matchers with PropertyChecks {
  
  val resolver = new AggregatorResolverImpl(this.getClass().getClassLoader())
  
  "AggregatorResolver" should "resolve correctly" in {
    val config = Json.obj()
    val rez = resolver.resolve(classOf[CorrectAggregator].getName(), config)
    rez shouldBe a [Success[_]]
    rez.get shouldBe a [CorrectAggregator]
  }
  
  
  it should "fail if class not found" in {
    val config = Json.obj()
    val rez = resolver.resolve("some.unexisted.class", config)
    rez shouldBe a [Failure[_]]
    rez.failed.get shouldBe a [ClassNotFoundException]    
  }
  
  
  it should "fail if constructor is incorrect" in {
    val config = Json.obj()
    val rez = resolver.resolve(classOf[AggregatorWithIncorrectConstructor].getName(), config)
    rez shouldBe a [Failure[_]]
    rez.failed.get shouldBe a [IllegalArgumentException]
    
  }
}