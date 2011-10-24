package nfp.view

import swing._
import event.TableRowsSelected

import org.squeryl.adapters.H2Adapter
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._
import util.Random
import nfp.DateConversion._
import collection.mutable.Set
import nfp.model.{Day, DataBase}
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.{TimeSeriesCollection, TimeSeries}
import org.jfree.chart.axis.{DateAxis, NumberAxis}
import org.jfree.data.time.{Day => JFDay}
import org.jfree.chart.renderer.xy.{XYSplineRenderer, XYLineAndShapeRenderer}
import java.awt.geom.Ellipse2D

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 25.06.11
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
object NFPCalculations {
  def roundTemperature(t: Float): Float = math.round(t * 20) / 20f
}

object Main extends App with Reactor {
  //init squeryl session factory
  Class.forName("org.h2.Driver");

  SessionFactory.concreteFactory = Some(() =>
    Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:~/.nfp-commander/data"),
      new H2Adapter))

  {
    val expectedDBVersion = "1"
    val foundDBVersion: Option[String] = DataBase.getProperty("db-version")
    assert(foundDBVersion == Some(expectedDBVersion),
      foundDBVersion
        .map("found db version %s, expected %s".format(_, expectedDBVersion))
        .getOrElse("could not find property 'db-version'")
    )
  }
  //  transaction {
  //    DataBase.create
  //    DataBase.putProperty("db-version", "1")
  //    println("created database")
  //  }

  val frame = new MainFrame
  frame.title = "nfp commander"

  val dayTableModel = new NFPTableModel
  val dayTable = new Table
  val dayEditor = new DayEditorPanel

  //dayTable.selection.elementMode = Table.ElementMode.Row
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
  DataBase.getTemperatureSeries.foreach{case (date, tOpt) =>
    tOpt.foreach{ temperature =>
      timeSeries.add(new JFDay(date), temperature)
    }
  }
  val numberAxis: NumberAxis = new NumberAxis("Temperatur/°C")
  numberAxis.setRange(35d,38d)
  val renderer = new DiscontinuedLineRenderer
  renderer.setSeriesShape(0,new Ellipse2D.Double(-3,-3,6,6))
  renderer.renderLinePredicate = timeData => timeData > 0 && (
    timeSeries.getDataItem(timeData - 1).getPeriod.asInstanceOf[JFDay].next == timeSeries.getDataItem(timeData).getPeriod
      )
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


  val panel = new MigPanel

  panel.add(dayEditor, "growy")
  panel.add(new ScrollPane(dayTable), "grow, wrap")
  panel.add(Component.wrap(chartPanel), "span")

  frame.contents = panel
  frame.open()

  frame.resizable = false
}

trait Editor extends Component {
  type TData

  def setValue(v: TData): Unit

  def getValue: TData
}