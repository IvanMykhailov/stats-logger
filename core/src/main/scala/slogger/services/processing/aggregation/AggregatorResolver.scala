package slogger.services.processing.aggregation

import scala.util.Try
import scala.util.Failure
import play.api.libs.json.JsObject
import scala.util.Success
import scala.util.control.NonFatal


trait AggregatorResolver {
  def resolve(aggregatorClass: String, config: JsObject): Try[Aggregator]
}


class AggregatorResolverImpl(
  aggregatorsClassLoader: ClassLoader     
) extends AggregatorResolver{
  
  override def resolve(aggregatorClass: String, config: JsObject): Try[Aggregator] = {
    Try {
      val clazz = aggregatorsClassLoader.loadClass(aggregatorClass)
      val constructors = clazz.getConstructors()
      if (constructors.length != 1) {
        throw new IllegalArgumentException("Aggregator class should have exactly one constructor")
      } else if (constructors(0).getParameterTypes().length != 1 || constructors(0).getParameterTypes()(0) != classOf[JsObject]) {
        throw new IllegalArgumentException("Aggregator constructor should accept config:JsObject")
      } else {
        constructors(0).newInstance(config).asInstanceOf[Aggregator]  
      }
    }    
  }
}