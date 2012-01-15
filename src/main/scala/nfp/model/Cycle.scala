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
import java.lang.String

/**
 * Representation class for data in day table. Only contains a comment and a start date. End date is defined
 * by the start date of the next cycle.
 */
case class Cycle(id: Date,
                 comment: Array[Byte]
           ) extends KeyedEntity[Date] {
  def lastDate: Date = DataBase.getCycleWithIndex(DataBase.cycleIndexOf(this) + 1)
    .map(_.id).getOrElse(
    new Date(new java.util.Date().getTime)
  )

  def this() = this(
    new java.sql.Date(0),
    Array.empty[Byte]
  )

  override def toString: String = "Cycle(%s, %s)".format(id, new String(comment, "UTF8"))
}


