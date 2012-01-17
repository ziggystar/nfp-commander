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

import java.awt.geom.Ellipse2D
import org.squeryl.PrimitiveTypeMode._
import org.jfree.chart.plot.XYPlot
import org.joda.time.DateTime
import swing.Component
import nfp.DateConversion._
import java.sql.Date
import nfp.model.{TableModifiedEvent, Day, DataBase}
import org.jfree.chart.{ChartMouseEvent, ChartMouseListener, ChartPanel, JFreeChart}
import org.jfree.chart.axis.{DateTickUnit, DateTickUnitType, DateAxis, NumberAxis}
import org.jfree.data.time.{DateRange, TimeSeriesDataItem, TimeSeriesCollection, TimeSeries, Day => JFDay}

/**
  * GUI component to display a NFP chart. It provides a view into the temperature series provided by the days table.
  * It updates its display if rows in this table change.
  *
  * @author Thomas Geier
  * Date: 25.06.11
  */
class NFPChart(private var beginDate: DateTime, private var endDate: DateTime) extends MigPanel {
  val timeSeries = new TimeSeries("Temperatur", "T [°C]", "Datum")

  class DaySeriesItem(val day: Day) extends TimeSeriesDataItem(new JFDay(day.id), day.temperature.getOrElse(0f))

  private def updateCache() {
    timeSeries.clear()
    transaction {
      from(DataBase.days)(d =>
        where((d.id gt (beginDate: Date)) and (d.id lt (endDate: Date)) and (d.temperature isNotNull))
          select(d)
      ).foreach{d => timeSeries.add(new DaySeriesItem(d))}
    }

    if(timeSeries.getItemCount <= 1){
      dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY,1))
    } else {
      dateAxis.setAutoTickUnitSelection(true)
    }

    dateAxis.setRange(new DateRange(beginDate: Date,endDate: Date),true,true)
  }

  val numberAxis: NumberAxis = new NumberAxis("Temperatur/°C")
  numberAxis.setRange(35d, 38d)
  val dateAxis: DateAxis = new DateAxis("Tag")
  val renderer = new DiscontinuedLineRenderer
  renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6))
  renderer.renderLinePredicate = timeData => timeData > 0 && (
  timeSeries.getDataItem(timeData - 1).getPeriod.asInstanceOf[JFDay].next == timeSeries.getDataItem(timeData).getPeriod
  )
  renderer.fillItemShape = seriesItem => !timeSeries.getDataItem(seriesItem).asInstanceOf[DaySeriesItem].day.ausklammern
  renderer.setDrawSeriesLineAsPath(false)
  val plot = new XYPlot(
    new TimeSeriesCollection(timeSeries),
    dateAxis,
    numberAxis,
    renderer
  )
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





