package nfp.view

import swing._
import javax.swing.table.AbstractTableModel
import org.squeryl.PrimitiveTypeMode._
import org.joda.time.LocalDate
import nfp.model.{Day, DataBase}
import nfp.DateConversion._

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 25.06.11
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
class NFPTableModel extends AbstractTableModel with Reactor {

  import DataBase._

  val names = List("Datum", "Temperatur", "Schleim", "MumuPos", "MumuOffen", "MumuHärte")

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
      case 1 => (if (rowQuery.ausklammern) "(%s)" else "%s") format prettifyOptionF(rowQuery.temperature map NFPCalculations.roundTemperature)
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
        try {
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





