package nfp.view

import swing._
import event.TableRowsSelected
import javax.swing.table.AbstractTableModel

import org.squeryl.adapters.H2Adapter
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._

import nfp.model.{Day, DataBase}
import org.joda.time.{DateTime, LocalDate}
import util.Random
import nfp.DateConversion._
import collection.mutable.Set

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
  implicit def dateTime2SQLDate(dt: LocalDate): java.sql.Date = new java.sql.Date(dt.toDateMidnight.getMillis)
  implicit def sqlDate2DateTime(dt: java.sql.Date): LocalDate = new LocalDate(dt.getTime)

  import DataBase._

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
        .map("found db version %s, expected %s".format(_,expectedDBVersion))
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
  frame.minimumSize = new Dimension(480,480)

  val tabbedPane = new TabbedPane

  val dayTableModel = new AbstractTableModel with Reactor {
    val names = List("Datum", "Temperatur", "Schleim", "MumuPos", "MumuOffen", "Kommentar" )

    private def createTransaction: IndexedSeq[Day] = {
      transaction {
        from(days)(d => where(d.id > ((new LocalDate).minusDays(131): java.sql.Date)) select (d) orderBy (d.id).desc).toIndexedSeq
      }
    }

    private var query = createTransaction

    override def getColumnName(p1: Int): String = names(p1)

    def prettyfyDate(d: LocalDate): String = {
      import org.joda.time.{Days => jtDays}
      jtDays.daysBetween(new LocalDate, d).getDays match {
        case 0 => "heute"
        case 1 => "gestern"
        case x => d.toString
        //case x => "%s %d Tagen".format(if(x > 0) "in" else "vor",math.abs(x))
      }

    }

    def getValueAt(row: Int, col: Int): AnyRef = {
      val rowQuery: Day = query(row)
      def prettifyOption[A](option: Option[A]): String = option.map(_.toString).getOrElse("-")
      def prettifyOptionF(option: Option[Float]): String = option.map("%.2f".format(_)).getOrElse("-")
      col match {
        case 0 => prettyfyDate(rowQuery.id)
        case 1 => (if(rowQuery.ausklammern) "(%s)" else "%s") format prettifyOptionF(rowQuery.temperature map NFPCalculations.roundTemperature)
        case 2 => prettifyOption(rowQuery.schleim)
        case 3 => prettifyOption(rowQuery.mumuPosition)
        case 4 => prettifyOption(rowQuery.mumuOpen)
        case 5 => prettifyOption(rowQuery.mumuFest)
      }
    }

    def getColumnCount: Int = 6

    def getRowCount: Int = query.size

    def getDayAt(row: Int): Day = query(row)

    reactions += {
      case DayModifiedEvent(day) => {
        transaction {
//          days.insertOrUpdate(day)
          try{
            days.update(day)
          } catch {
            case x => days.insert(day)
          }
        }
        query = createTransaction
        this.fireTableDataChanged()
      }
    }
  }

  val dayTable = new Table
  //dayTable.selection.elementMode = Table.ElementMode.Row
  dayTable.reactions += {
    case e: TableRowsSelected =>
      val selection: Set[Int] = dayTable.selection.cells.map(_._1)
      if(selection.size == 1)
        dayEditor.setContent(dayTableModel.getDayAt(selection.min))
  }
  dayTable.listenTo(dayTable.selection)

  dayTable.model = dayTableModel

  tabbedPane.pages += new TabbedPane.Page("Tabelle", new ScrollPane(dayTable))


  val dayEditor = new DayEditorPanel
  dayTableModel.listenTo(dayEditor)

  val contentPane = new BoxPanel(Orientation.Vertical) {
    contents += tabbedPane
    contents += dayEditor
  }
  frame.contents = contentPane
  frame.open()
}

trait Editor extends Component {
  type TData
  def setValue(v: TData): Unit
  def getValue: TData
}