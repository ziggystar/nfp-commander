/*
 * Copyright (C) 2012 thomas
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

import nfp.DateConversion._
import swing._
import javax.swing.JOptionPane
import org.joda.time.DateMidnight
import nfp.model.{DataBase, Day, Cycle}

class ChartPage(_cycle: Cycle) extends MigPanel {

  private val cycleTracker = new CycleTracker(_cycle)

  val chart = new NFPChart(endDate = cycleTracker.get.lastDate, beginDate = cycleTracker.get.id)

  this.listenTo(cycleTracker)
  this.reactions += {
    case cycleTracker.EntityChangedEvent(c) => {
      updatePlotRanges()
    }
  }
  this.add(chart, "ax center, grow,push, wrap")
  this.add(cycleTracker.createSkipPrevButton("Letzter Zyklus"),"split 3")
  this.add(cycleTracker.createSkipNextButton("Nächster Zyklus"))
  this.add(createAddDayButton)

  def createOrModifyDayDialog(parent: java.awt.Component, day: Day): Option[Day] = {
    val editor: DayEditorPanel = new DayEditorPanel(day)
    val pane = new JOptionPane(editor.peer, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION)
    val  dialog = pane.createDialog(parent, "Tag erstellen/ändern")
    dialog.setVisible(true)
    val selectedValue = pane.getValue
    selectedValue.asInstanceOf[Int] match {
      case JOptionPane.OK_OPTION => editor.getDay
      case _ => None
    }
  }

  private def createAddDayButton: Button = {
    val defaultToDay: Day = (new Day()).copy(
      id = DateMidnight.now.toDateTime,
      blutung = Some("keine"),
      sex = Some("keiner")
    )
    new Button(Action("Neuer Tag")(
      createOrModifyDayDialog(this.peer, defaultToDay).foreach(DataBase.createOrUpdateDay _))
    )
  }

  private def updatePlotRanges() {
    chart.setRange(cycleTracker.get.id, cycleTracker.get.lastDate)
  }

  def setNewCycle(newCycle: Cycle) {
    cycleTracker.set(newCycle)
  }
}