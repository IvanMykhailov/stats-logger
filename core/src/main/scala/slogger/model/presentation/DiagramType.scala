package slogger.model.presentation

object DiagramType extends Enumeration {
  val Line, Percent = Value
    
  def parse(s: String) = values.find(_.toString.toLowerCase() == s.toLowerCase())
  
  def parseStrict(s: String): Value = parse(s).getOrElse(
    throw new Exception(s"Incorrect DiagramType value '$s'. Allowed values: " + values.mkString(", "))
  )
}