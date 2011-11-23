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
import org.squeryl.internals.DatabaseAdapter
import swing.Publisher
import swing.event.Event

/** This holds the layout for the database. Particularly it specifies all tables.
  *
  * @author Thomas Geier
  */
object DataBase extends Schema {
  case class DayModifiedEvent(newValue: Day) extends Event
  val dayModified: Publisher = new Publisher{}

  //see the file doc/db-versions.md
  val currentVersion = "2"

  /** The days table holds NFP data associated to a particular day.
    * @see Day
    */
  val days = table[Day]

  val cycles = table[Cycle]

  /** The properties table holds key/value pairs like options or database layout versions.
    */
  val properties = table[KeyValue]

  /** Store a key/value pair in the properties table.
    */
  def putProperty(key: String, value: String) = transaction{
    try {
      properties.insertOrUpdate(new KeyValue(key, value))
    } catch {
      case x => properties.update(new KeyValue(key, value))
    }
  }

  /** Retrieve a value for a given key from the properties table.
    */
  def getProperty(key: String): Option[String] = transaction{
    from(properties)(p => where(p.id === key) select p.value).headOption
  }

  def getDayAtDate(date: Date): Option[Day] = transaction {
    from(days)(d => where(d.id === date) select d).headOption
  }

  def createOrUpdateDay(day: Day) {
    transaction {
      try {
        days.update(day)
      } catch {
        case x => days.insert(day)
      }
    }
    dayModified.publish(DayModifiedEvent(day))
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
      foundDBVersion match {
        case None => {
          System.err.println("could not retrieve db version from properties table")
          System.exit(-1)
        }
        case Some("1") => migrate1to2()
        case Some("2") => //everything's fine
        case Some(v) => {
          System.err.println("found db version %s, expected %s".format(v, currentVersion))
          System.exit(-1)
        }
      }
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

  def migrate1to2() {
    transaction {
      val dbAdapt: DatabaseAdapter = Session.currentSession.databaseAdapter
      val statementW = dbAdapt.string2StatementWriter("")
      dbAdapt.writeCreateTable(cycles, statementW, this)
      val alterResult = dbAdapt.executeUpdate(Session.currentSession, statementW)
      println("updated db layout from version 1 to 2:\n%s".format(alterResult))
    }
    putProperty("db-version", "2")
  }
}

