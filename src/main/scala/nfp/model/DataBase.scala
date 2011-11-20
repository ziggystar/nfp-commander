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

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.{Session, SessionFactory, Schema}
import org.squeryl.adapters.H2Adapter
import java.sql.Date

/** This holds the layout for the database. Particularly it specifies all tables.
  *
  * @author Thomas Geier
  */
object DataBase extends Schema {

  //see the file doc/db-versions.md
  val currentVersion = "1"

  /** The days table holds NFP data associated to a particular day.
    * @see Day
    */
  val days = table[Day]

  /** The properties table holds key/value pairs like options or database layout versions.
    */
  val properties = table[KeyValue]

  /** Store a key/value pair in the properties table.
    */
  def putProperty(key: String, value: String) = transaction{properties.insertOrUpdate(new KeyValue(key, value))}

  /** Retrieve a value for a given key from the properties table.
    */
  def getProperty(key: String): Option[String] = transaction{
    from(properties)(p => where(p.id === key) select p.value).headOption
  }

  def getDayAtDate(date: Date): Option[Day] = transaction {
    from(days)(d => where(d.id === date) select d).headOption
  }

  /** Do everything necessary after application start to use the database. Also check that the db layout is current.
    */
  def initDB(url: String) {
    Class.forName("org.h2.Driver");

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection(url),
        new H2Adapter))

    {
      val foundDBVersion: Option[String] = DataBase.getProperty("db-version")
      assert(foundDBVersion == Some(currentVersion),
        foundDBVersion
          .map("found db version %s, expected %s".format(_, currentVersion))
          .getOrElse("could not find property 'db-version'")
      )
    }
  }

  /** Create the tables with the current layout.
    */
  def createTables() {
    transaction {
      DataBase.create
      DataBase.putProperty("db-version", currentVersion)
    }
  }
}


