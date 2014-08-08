package test.processing.calc

import test.processing.BaseDaoTest
import slogger.services.processing.Calculator


class BaseCalculationTest extends BaseDaoTest with ReferenceResults {

  val calculator = Calculator.create(dbProvider)
  
  def check(reference: Map[String, Double])(rez: Map[String, BigDecimal]): Unit = {
    reference.keySet shouldBe rez.keySet
    reference.toSeq.foreach { case (k, v) => 
      rez(k).toDouble shouldBe v +- 0.0000000001 
    }    
  }
}