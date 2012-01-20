/*
 * Copyright (C) 2011 thomas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nfp.view

/*
 * Copyright (C) 2011 Thomas Geier
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.squeryl.PrimitiveTypeMode._
import org.joda.time.DateTime
import swing.Component
import nfp.DateConversion._
import java.sql.Date
import nfp.model.{TableModifiedEvent, Day, DataBase}
import org.jfree.chart.{ChartMouseEvent, ChartMouseListener, ChartPanel, JFreeChart}
import java.awt.geom.Ellipse2D
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.axis._
import collection.immutable.Map
import java.lang.String
import org.jfree.data.time.{TimeSeriesCollection, DateRange, TimeSeriesDataItem, TimeSeries, Day => JFDay}

/**
  * GUI component to display a NFP chart. It provides a view into the temperature series provided by the days table.
  * It updates its display if rows in this table change.
  *
  * @author Thomas Geier
  * Date: 25.06.11
  */
class NFPChart(private var beginDate: DateTime, private var endDate: DateTime) extends MigPanel {
  val temperatureSeries = new TimeSeries("Temperatur", "T [°C]", "Datum")
  val bloodSeries = new TimeSeries("Blutung")
  val bloodMapping: Map[String, Double] = Map(
     "keine" -> 0d,
     "leicht" -> 1d,
     "mittel" -> 2d,
     "stark" -> 3d,
     "Schmier" -> 4d
  ).withDefaultValue(0d)

  class DaySeriesItem(val day: Day, value: Double) extends TimeSeriesDataItem(new JFDay(day.id), value)

  private def updateCache() {
    temperatureSeries.clear()
    bloodSeries.clear()

    transaction {
      from(DataBase.days)(d =>
        where((d.id gte (beginDate: Date)) and (d.id lte (endDate: Date)))
          select(d)
      ).foreach{d =>
        //add to the corresponding series if the value is defined
        d.temperature.foreach(t => temperatureSeries.add(new DaySeriesItem(d,t)))
        d.blutung.foreach(bl => bloodSeries.add(new DaySeriesItem(d,bloodMapping(bl))))
      }
    }
    //set this every time as a workAround that you can zoom by left dragging (probably bug?
    numberAxis.setRange(35.5d, 37.5d)
    dateAxis.setRange(new DateRange(beginDate.minusDays(1): Date,endDate.plusDays(1): Date),true,true)
  }

  val numberAxis: NumberAxis = new NumberAxis("Temperatur/°C")
  val dateAxis: DateAxis = new DateAxis("Tag")
  val renderer = new DiscontinuedLineRenderer
  renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6))
  renderer.renderLinePredicate = timeData => timeData > 0 && (
  temperatureSeries.getDataItem(timeData - 1).getPeriod.asInstanceOf[JFDay].next == temperatureSeries.getDataItem(timeData).getPeriod
  )
  renderer.fillItemShape = seriesItem => !temperatureSeries.getDataItem(seriesItem).asInstanceOf[DaySeriesItem].day.ausklammern
  renderer.setDrawSeriesLineAsPath(false)
  val plot = new XYPlot(
    new TimeSeriesCollection(temperatureSeries),
    dateAxis,
    numberAxis,
    renderer
  )

  plot.setDataset(1,new TimeSeriesCollection(bloodSeries))
  plot.setRenderer(1,new XYCategoryRenderer(37d))

  val chart: JFreeChart = new JFreeChart(plot)
  chart.removeLegend()

  val chartPanel = new ChartPanel(chart)
  chartPanel.setFillZoomRectangle(false)
  chartPanel.setRangeZoomable(false)
  chartPanel.setPopupMenu(null)

  //add listener for selecting points on the chart
  chartPanel.addChartMouseListener(new ChartMouseListener{
    def chartMouseClicked(p1: ChartMouseEvent) {
      println(p1 + "/" + p1.getEntity + "/" + p1.getTrigger)
    }

    def chartMouseMoved(p1: ChartMouseEvent) {}
  })

  this.listenTo(DataBase.modifications)
  this.reactions += {
    case TableModifiedEvent(table) if(table == DataBase.days) => updateCache()
  }

  this.add(Component.wrap(chartPanel))

  updateCache()

  def setRange(begin: DateTime, end: DateTime) {
    beginDate = begin
    endDate = end
    updateCache()
  }
}