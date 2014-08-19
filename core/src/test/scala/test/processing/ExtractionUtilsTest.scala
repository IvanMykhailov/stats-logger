package test.processing

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import play.api.libs.json.Json
import slogger.services.processing.extraction.ExtractionUtils
import play.api.libs.json.JsString


class ExtractionUtilsTest extends FlatSpec with Matchers {
  
  "ExtractionUtils" should "extract nested filed" in {
    val obj = Json.obj(
      "level1" -> Json.obj(
        "level2" -> Json.obj(
          "data" -> "myValue"    
        )    
      )    
    )
    
    ExtractionUtils.extractByPath(obj, "level1.level2.data") shouldBe JsString("myValue")
  }
}