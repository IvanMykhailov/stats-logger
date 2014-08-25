package slogger.model.presentation

import play.api.libs.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.Duration
import org.joda.time.DateTimeConstants


case class HighchartLineDiagramData(
  xAxisLabels: Seq[DateTime], 
  series: Seq[HighchartSeries],
  sliceDuration: Duration
) {
  val dateFormatter = DateTimeFormat.forPattern("MMM dd")
  val dateTimeFormatter = DateTimeFormat.forPattern("MMM dd, HH:mm")
  val timeFormatter = DateTimeFormat.forPattern("HH:mm")
  
  
  def xAxisLabelsJson: JsValue = {
    val sliceMillis = sliceDuration.getMillis()
    val totalMillis = sliceDuration.getMillis() * xAxisLabels.length
    
    val needTime = (sliceMillis < DateTimeConstants.MILLIS_PER_DAY.longValue)
    val needDate = (totalMillis > DateTimeConstants.MILLIS_PER_DAY.longValue + DateTimeConstants.MILLIS_PER_HOUR.longValue)
    
    val formatter = (needDate, needTime) match {
      case (true, true)   => dateTimeFormatter
      case (true, false)  => dateFormatter
      case (false, true)  => timeFormatter
      case (false, false) => dateTimeFormatter// WTF?
    }
    
    Json.toJson(xAxisLabels.map(formatter.print(_)))
  }
  
  
  def seriesJson: JsValue = {
    import HighchartLineDiagramData.HighchartSeriesFormat
    Json.toJson(series)
  }
}


case class HighchartSeries(
  name: String,
  data: Seq[HighchartSeriesPoint],
  yAxis: Int = 1
)


case class HighchartSeriesPoint(
  y: BigDecimal
)


object HighchartLineDiagramData {
  import slogger.model.common.JsonFormats._
  
  implicit val HighchartSeriesPointFormat: Format[HighchartSeriesPoint] = Json.format[HighchartSeriesPoint]
  
  implicit val HighchartSeriesFormat: Format[HighchartSeries] = Json.format[HighchartSeries]
  
  implicit val HighchartLineDiagramDataFormat: Format[HighchartLineDiagramData] = Json.format[HighchartLineDiagramData]
}