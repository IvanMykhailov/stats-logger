package slogger.services.helpers.recalculation

import slogger.model.specification.CalculationSpecs
import org.joda.time.Duration


trait RecalculableSpecs {
  def id: String
  def name: String
  def calculationSpecs: CalculationSpecs    
  def recalculationTime: Duration
}