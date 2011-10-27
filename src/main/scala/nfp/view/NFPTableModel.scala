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
import javax.swing.table.AbstractTableModel
import org.squeryl.PrimitiveTypeMode._
import org.joda.time.LocalDate
import nfp.model.{Day, DataBase}
import nfp.DateConversion._

/** Table model for use with Swing GUI tables.
  *
  * @author Thomas Geier
  */
class NFPTableModel extends AbstractTableModel with Reactor {

  import DataBase._

  val names = List("Datum", "Temperatur", "Schleim", "Mumu Pos", "Mumu Öffnung", "Mumu Härte")

  private def createTransaction: IndexedSeq[Day] = {
    transaction {
      from(days)(d => select(d) orderBy (d.id).desc).toIndexedSeq
    }
  }

  private var query = createTransaction

  override def getColumnName(p1: Int): String = names(p1)

  def prettifyDate(d: LocalDate): String = {
    import org.joda.time.{Days => jtDays}
    jtDays.daysBetween(new LocalDate, d).getDays match {
      case 0 => "heute"
      case 1 => "gestern"
      case x => d.toString
    }

  }

  def getValueAt(row: Int, col: Int): AnyRef = {
    val rowQuery: Day = query(row)
    def prettifyOption[A](option: Option[A]): String = option.map(_.toString).getOrElse("")
    col match {
      case 0 => prettifyDate(rowQuery.id)
      case 1 => rowQuery.temperature.map(t => (if (rowQuery.ausklammern) "(%.2f°C)" else "%.2f°C") format t).getOrElse("")
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





