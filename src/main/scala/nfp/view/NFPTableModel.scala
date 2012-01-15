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

import swing.{Table => _, _}
import org.squeryl.PrimitiveTypeMode._
import org.joda.time.LocalDate
import nfp.model.{Day, DataBase}
import nfp.DateConversion._

/** Table model for use with Swing GUI tables.
  *
  * @author Thomas Geier
  */
class NFPTableModel extends DBTableModel(
  DataBase.days,
  from(DataBase.days)(d => select(d) orderBy(d.id asc))
) with Reactor {

  val names = List("Datum", "Temperatur", "Schleim", "Mumu Pos", "Mumu Öffnung", "Mumu Härte", "Blutung", "Sex")

  override def getColumnName(p1: Int): String = names(p1)

  def prettifyDate(d: LocalDate): String = {
    import org.joda.time.{Days => jtDays}
    jtDays.daysBetween(new LocalDate, d).getDays match {
      case 0 => "heute"
      case 1 => "gestern"
      case x => d.toString
    }
  }

  def getColumnOfRow(day: Day, col: Int): AnyRef = {
    def prettifyOption[A](option: Option[A]): String = option.map(_.toString).getOrElse("")
    col match {
      case 0 => prettifyDate(day.id)
      case 1 => day.temperature.map(t => (if (day.ausklammern) "(%.2f°C)" else "%.2f°C") format t).getOrElse("")
      case 2 => prettifyOption(day.schleim)
      case 3 => prettifyOption(day.mumuPosition)
      case 4 => prettifyOption(day.mumuOpen)
      case 5 => prettifyOption(day.mumuFest)
      case 6 => prettifyOption(day.blutung)
      case 7 => prettifyOption(day.sex)
    }
  }

  def getColumnCount: Int = 8
}





