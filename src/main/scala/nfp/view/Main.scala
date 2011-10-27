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

package nfp.view

import swing._
import event.TableRowsSelected

import org.squeryl.PrimitiveTypeMode._
import collection.mutable.Set
import nfp.model.{Day, DataBase}
import org.jfree.chart.{ChartPanel, JFreeChart}
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.axis.{DateAxis, NumberAxis}
import java.awt.geom.Ellipse2D
import org.jfree.data.time.{TimeSeriesDataItem, TimeSeriesCollection, TimeSeries, Day => JFDay}

/**
  * The main executable.
  * Connecting to DB, build the GUI.
  *
  * @author Thomas Geier
  * Date: 25.06.11
  */
object NFPCalculations {
  def roundTemperature(t: Float): Float = math.round(t * 20) / 20f
}

object Main extends App with Reactor {
  //init squeryl session factory
  DataBase.initDB("jdbc:h2:~/.nfp-commander/data")

  val frame = new MainFrame
  frame.title = "nfp commander"

  val dayTableModel = new NFPTableModel
  val dayTable = new Table
  val dayEditor = new DayEditorPanel

  dayTable.reactions += {
    case e: TableRowsSelected =>
      val selection: Set[Int] = dayTable.selection.cells.map(_._1)
      if (selection.size == 1)
        dayEditor.setContent(dayTableModel.getDayAt(selection.min))
  }
  dayTable.listenTo(dayTable.selection)
  dayTable.model = dayTableModel
  dayTableModel.listenTo(dayEditor)

  val timeSeries = new TimeSeries("Temperatur", "T [°C]", "Datum")

  def chartUpdateItem(day: Day) {
    val item: DaySeriesItem = new DaySeriesItem(day)
    timeSeries.delete(item.getPeriod)
    timeSeries.add(item, true)
  }

  class DaySeriesItem(val day: Day) extends TimeSeriesDataItem(new JFDay(day.id), day.temperature.getOrElse(0f))

  transaction {
    DataBase.days.iterator.filter(_.temperature.isDefined).map(new DaySeriesItem(_)).foreach(timeSeries.add)
  }

  val numberAxis: NumberAxis = new NumberAxis("Temperatur/°C")
  numberAxis.setRange(35d, 38d)
  val renderer = new DiscontinuedLineRenderer
  renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6))
  renderer.renderLinePredicate = timeData => timeData > 0 && (
    timeSeries.getDataItem(timeData - 1).getPeriod.asInstanceOf[JFDay].next == timeSeries.getDataItem(timeData).getPeriod
    )
  renderer.fillItemShape = seriesItem => !timeSeries.getDataItem(seriesItem).asInstanceOf[DaySeriesItem].day.ausklammern
  renderer.setDrawSeriesLineAsPath(false)
  val plot = new XYPlot(
    new TimeSeriesCollection(timeSeries),
    new DateAxis("Tag"),
    numberAxis,
    renderer
  )
  val chart: JFreeChart = new JFreeChart(plot)
  chart.removeLegend()
  val chartPanel = new ChartPanel(chart)
  chartPanel.setDomainZoomable(false)
  chartPanel.setFillZoomRectangle(false)
  chartPanel.setPopupMenu(null)

  this.listenTo(dayEditor)
  this.reactions += {
    case DayModifiedEvent(d) => chartUpdateItem(d)
  }
  val panel = new MigPanel

  panel.add(dayEditor, "h 300, growy")
  panel.add(Component.wrap(chartPanel), "h 300, grow, push, wrap, left")
  panel.add(new ScrollPane(dayTable), "span, h 180, grow, push")

  frame.contents = panel
  frame.open()
}