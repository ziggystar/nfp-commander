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

package nfp.model

import java.sql.Date
import org.squeryl.KeyedEntity

/**
 * Representation class for data in day table.
 */
case class Cycle(id: Date,
                 comment: Array[Byte]
           ) extends KeyedEntity[Date] {
  def this() = this(
    new java.sql.Date(0),
    Array.empty[Byte]
  )
}


