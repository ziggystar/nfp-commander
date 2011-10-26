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

import org.squeryl.Schema
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._
import java.sql.Date

object DataBase extends Schema {
  val days = table[Day]
  val properties = table[KeyValue]

  def putProperty(key: String, value: String) = transaction{properties.insertOrUpdate(new KeyValue(key, value))}
  def getProperty(key: String): Option[String] = transaction{
    from(properties)(p => where(p.id === key) select p.value).headOption
  }
}


