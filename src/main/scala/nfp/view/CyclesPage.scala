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

import java.util.Date
import swing._
import com.toedter.calendar.JCalendar
import event.{MousePressed, ButtonClicked}
import nfp.DateConversion._
import nfp.model.{Cycle, DataBase}
import org.squeryl.PrimitiveTypeMode._

/**
 * This GUI widget lets you create/delete and modify cycles.
 *
 * @author Thomas Geier
 * Date: 25.11.11
 */

class CyclesPage extends MigPanel {
  object CyclesTM extends DBTableModel[Cycle](
    DataBase.cycles,
    from(DataBase.cycles)(c => select(c) orderBy(c.id asc))
  ) {
    override def getColumnName(p1: Int): String = if(p1 == 0) "Beginn" else "Kommentar"
    def getColumnOfRow(cycle: Cycle, col: Int): AnyRef = if(col == 0) cycle.id else new String(cycle.comment)
    def getColumnCount: Int = 2
  }

  private val dateChooser = new JCalendar(new Date)
  private val commentField = new TextArea(7,80)
  val commentScrollPane: ScrollPane = new ScrollPane(commentField)

  val table = new Table
  table.model = CyclesTM

  def tablePopMenu(cycle: Cycle) = new PopupMenu {
    contents += new MenuItem(Action("löschen")(DataBase.removeCycle(cycle.id)))
  }

  table.listenTo(table.mouse.clicks)
  table.reactions += {
    case MousePressed(src,point,modifiers,clicks,triggers) if(triggers) => {
      val row: Int = table.peer.rowAtPoint(point)
      val selectedCycle: Cycle = CyclesTM.getRowAt(row)
      tablePopMenu(selectedCycle).show(src,point.x,point.y)
    }
  }

  val ButtonCreate = new Button("Zyklus Erstellen")

  commentScrollPane.horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never

  this.add(new ScrollPane(table), "span 2,growx,wrap")
  this.add(Component.wrap(dateChooser))
  this.add(commentScrollPane, "wrap")

  this.add(ButtonCreate)


  this.listenTo(ButtonCreate)
  this.listenTo(DataBase.modifications)
  this.reactions += {
    case swing.event.ActionEvent(ButtonCreate) => DataBase.addCycle(editedCycle)
  }

  def editedCycle = Cycle(dateChooser.getDate, commentField.text.getBytes("UTF8"))
}