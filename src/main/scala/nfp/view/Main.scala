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
import org.squeryl.PrimitiveTypeMode._
import swing.TabbedPane.Page
import nfp.model._
import collection.immutable.Map
import javax.swing.ImageIcon
import nfp.view.I18n._

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
    frame.title = 'title

    val dayTableModel = new NFPTableModel
    val dayTable = new Table

    dayTable.listenTo(dayTable.selection)
    dayTable.model = dayTableModel

    val chartPage = new ChartPage(DataBase.currentCycle)
    val dayTablePage = new MigPanel
    val cyclesPage = new CyclesPage

    dayTablePage.add(new ScrollPane(dayTable), "growx,growy,push")

    val tabbedPane = new TabbedPane
    tabbedPane.pages += new Page('chart, chartPage)
    tabbedPane.pages += new Page('table, dayTablePage)
    tabbedPane.pages += new Page('cycles, cyclesPage)
    tabbedPane.pages += new Page('options, new Label("not yet"))

    tabbedPane.tabPlacement(Alignment.Left)

    val tabIcons: Map[Int, ImageIcon] = Seq(
      0 -> ("tab-curve.png", 'chart),
      1 -> ("tab-table.png", 'table),
      2 -> ("tab-cycles.png", 'cycles),
      3 -> ("tab-options.png", 'options)
    )
      .map{case (idx,(file,desc)) => idx -> new javax.swing.ImageIcon(this.getClass.getResource(file),desc)}.toMap //turn into an URL

    tabIcons.foreach{case (idx,icon) =>
      tabbedPane.peer.setTitleAt(idx,null)
      tabbedPane.peer.setToolTipTextAt(idx,icon.getDescription)
      tabbedPane.peer.setIconAt(idx,icon)
    }

    frame.contents = tabbedPane
    frame.open()
  }
}