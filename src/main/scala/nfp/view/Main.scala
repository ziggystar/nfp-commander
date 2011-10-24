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

  val panel = new MigPanel

  panel.add(new ScrollPane(dayTable))
  panel.add(dayEditor, "grow, wrap")

  frame.contents = panel
  frame.open()

  frame.resizable = false
}

trait Editor extends Component {
  type TData

  def setValue(v: TData): Unit

  def getValue: TData
}