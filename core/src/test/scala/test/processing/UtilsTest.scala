package test.processing

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import play.api.libs.json._
import play.api.libs.json.Json.{obj, arr}
import slogger.services.processing.extraction.ExtractionUtils
import play.api.libs.json.JsString
import slogger.services.processing.aggregation.aggregators.AggregatorUtils


class UtilsTest extends FlatSpec with Matchers {
  
  "ExtractionUtils" should "extract nested filed" in {
    val o = obj(
      "level1" -> obj(
        "level2" -> obj(
          "data" -> "myValue"    
        )    
      )    
    )
    ExtractionUtils.extractByPath(o, "level1.level2.data") shouldBe JsString("myValue")
  }
  
  "AggregatorUtils" should "extract values from array" in {
    val o: JsValue = obj("data" -> obj(
      "APs" -> arr("ap1", "ap2")
    ))
    AggregatorUtils.stringValues(ExtractionUtils.extractByPath(o, "data.APs")) shouldBe Seq("ap1", "ap2")
  }
  
  "AggregatorUtils" should "extract keys from object" in {
    val o: JsValue = obj("data" -> obj(
      "APs" -> obj(
        "ap1" -> 3,
        "ap2" -> 5
      )
    ))
    AggregatorUtils.stringValues(ExtractionUtils.extractByPath(o, "data.APs")) shouldBe Seq("ap1", "ap2")
  }
  
}