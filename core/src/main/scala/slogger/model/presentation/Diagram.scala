package model.entity.presentation

import slogger.model.presentation.HighchartLineDiagramData


case class Diagram(
  data: HighchartLineDiagramData,  
  diagramType: DiagramType.Value,
  
  width: Int = 1024,
  height: Int = 512
) {
  
  def seriesType: String = diagramType match {
    case DiagramType.Line => "line"
    case DiagramType.Percent => "area"
  }
  
  def stacking: String = diagramType match {
    case DiagramType.Line => ""
    case DiagramType.Percent => "percent"
  }
  
}