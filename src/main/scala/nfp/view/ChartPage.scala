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

import nfp.model.Cycle
import nfp.DateConversion._

class ChartPage(_cycle: Cycle) extends MigPanel {

  private val cycleTracker = new CycleTracker(_cycle)

  val dayEditor = new DayEditorPanel
  val chart = new NFPChart(endDate = cycleTracker.get.lastDate, beginDate = cycleTracker.get.id)

  this.listenTo(cycleTracker)
  this.reactions += {
    case cycleTracker.EntityChangedEvent(c) => {
      updatePlotRanges()
    }
  }
  this.add(dayEditor, "aligny top")
  this.add(chart, "spany 2, wrap")
  this.add(cycleTracker.createSkipPrevButton("Letzter Zyklus"),"split 2")
  this.add(cycleTracker.createSkipNextButton("Nächster Zyklus"), "wrap")

  private def updatePlotRanges() {
    chart.setRange(cycleTracker.get.id, cycleTracker.get.lastDate)
  }

  def setNewCycle(newCycle: Cycle) {
    cycleTracker.set(newCycle)
  }
}