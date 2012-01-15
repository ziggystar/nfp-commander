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

import org.squeryl.{Query, Table}
import swing.Reactor
import scala.{Int, AnyRef}
import nfp.model.DataBase
import nfp.model.TableModifiedEvent
import javax.swing.table.AbstractTableModel

/** Provides common functionality for a TableModel that is backed by a squeryl table. */
abstract class DBTableModel[T](val table: Table[T], val query: Query[T]) extends AbstractTableModel with Reactor {
  import org.squeryl.PrimitiveTypeMode._

  private var cache: IndexedSeq[T] = _

  updateCache()

  def getColumnCount: Int

  def getColumnOfRow(row: T, col: Int): AnyRef

  def getValueAt(row: Int, col: Int): AnyRef = getColumnOfRow(getRowAt(row: Int), col)

  def getRowCount: Int = cache.size
  def getRowAt(row: Int): T = cache(row)

  private def updateCache() {cache = inTransaction{query.toIndexedSeq}}

  this.listenTo(DataBase.modifications)
  reactions += {
    case tme: TableModifiedEvent[T] if (tme.affectedTable == table) => {
      updateCache()
      this.fireTableDataChanged()
    }
  }
}