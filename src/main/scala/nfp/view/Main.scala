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

import collection.mutable.Set
import swing._
import event.TableRowsSelected
import org.squeryl.PrimitiveTypeMode._
import swing.TabbedPane.Page
import nfp.model._

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

object Main extends Reactor {

  def main(args: Array[String]) {
    //init squeryl session factory
    DataBase.initDB()

    //handle options
    if (args.contains("--printDDL")) {
      transaction {
        DataBase.printDdl
      }
      System.exit(0)
    }

    val frame = new MainFrame
    frame.title = "nfp commander"

    val dayTableModel = new NFPTableModel
    val dayTable = new Table
    val dayEditor = new DayEditorPanel

    dayTable.reactions += {
      case e: TableRowsSelected =>
        val selection: Set[Int] = dayTable.selection.cells.map(_._1)
        if (selection.size == 1)
          dayEditor.setContent(dayTableModel.getRowAt(selection.min))
    }
    dayTable.listenTo(dayTable.selection)
    dayTable.model = dayTableModel

    val chartPage = new ChartPage(DataBase.currentCycle)
    val dayTablePage = new MigPanel
    val cyclesPage = new CyclesPage

    dayTablePage.add(dayEditor, "aligny top")
    dayTablePage.add(new ScrollPane(dayTable), "growy, growx, push")

    val tabbedPane = new TabbedPane
    tabbedPane.pages += new Page("Kurve", chartPage)
    tabbedPane.pages += new Page("Tage", dayTablePage)
    tabbedPane.pages += new Page("Zyklen", cyclesPage)
    frame.contents = tabbedPane
    frame.open()
  }
}