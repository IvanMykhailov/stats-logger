package slogger.services.processing.presentation

import slogger.model.presentation.HighchartLineDiagramData
import slogger.model.processing.StatsResult
import com.github.nscala_time.time.Imports._
import slogger.model.presentation.HighchartSeriesPoint
import slogger.model.presentation.HighchartSeries


object HighchartDiagramDataConverter {
  
  val MaxDisplayedSeriesCount = 15
  
  
  def convert(statsResult: StatsResult, sliceDuration: Duration): HighchartLineDiagramData = {
    val linesOrdered = statsResult.lines.sortBy(_.slice.end)
  
    val xAxisLabels = linesOrdered.map(_.slice.end)
    
    val seriesNames = linesOrdered.foldLeft(Set.empty[String]) { case (set, sliceRez) =>
      set ++ sliceRez.results.keys.toSet 
    }
        
    val series = seriesNames.toSeq.map { name => 
      val seriesData = linesOrdered.map { sliceRez =>
        val value = sliceRez.results.get(name).getOrElse(BigDecimal(0))
        HighchartSeriesPoint(value)
      }
      HighchartSeries(name, seriesData)
    }
    
    val seriesToDisplay = if (series.length > MaxDisplayedSeriesCount) {
      val mostImportantSeries =
        series.map(oneSeries => (oneSeries.name, oneSeries.data.map(_.y).max))
        .sortBy(_._2).reverse.take(MaxDisplayedSeriesCount)
        .map(_._1)
        .toSet      
      series.filter(s => mostImportantSeries.contains(s.name))
    } else {
      series
    }
    
    HighchartLineDiagramData(xAxisLabels, seriesToDisplay, sliceDuration)
  }
}