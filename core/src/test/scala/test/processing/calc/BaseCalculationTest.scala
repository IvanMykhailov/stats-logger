package test.processing.calc

import test.processing.BaseDaoTest
import slogger.services.processing.Calculator


class BaseCalculationTest extends BaseDaoTest with ReferenceResults {

  val calculator = Calculator.create(dbProvider)
}