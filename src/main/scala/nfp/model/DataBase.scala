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
import org.squeryl.adapters.H2Adapter
import java.sql.Date
import org.squeryl.internals.DatabaseAdapter
import swing.Publisher
import swing.event.Event
import org.joda.time.DateTime
import nfp.DateConversion._
import org.squeryl._

/** This holds the layout for the database. Particularly it specifies all tables.
  *
  * @author Thomas Geier
  */
object DataBase extends Schema {

  Class.forName("org.h2.Driver")
  val dbUrl = "jdbc:h2:~/.nfp-commander/data"

//
//  initDB("jdbc:h2:~/.nfp-commander/data")

  val modifications: Publisher = new Publisher{}

  //see the file doc/db-versions.md
  val currentVersion = "2"

  /** The days table holds NFP data associated to a particular day.
    * @see Day
    */
  val days: Table[Day] = table[Day]

  val cycles: Table[Cycle] = table[Cycle]

  /** The properties table holds key/value pairs like options or database layout versions.
    */
  val properties = table[KeyValue]

  /** Store a key/value pair in the properties table.
    */
  def putProperty(key: String, value: String) = inTransaction{
    try {
      properties.insertOrUpdate(new KeyValue(key, value))
    } catch {
      case x => properties.update(new KeyValue(key, value))
    }
  }

  /** Retrieve a value for a given key from the properties table.
    */
  def getProperty(key: String): Option[String] = inTransaction{
    from(properties)(p => where(p.id === key) select p.value).headOption
  }

  def getDayAtDate(date: Date): Option[Day] = inTransaction {
    from(days)(d => where(d.id === date) select d).headOption
  }

  def getFirstDay: Option[Day] = inTransaction {
    from(days)(d => select(d)).headOption
  }

  def getCycles: List[Cycle] = inTransaction{
    from(cycles)(c => select(c) orderBy(c.id asc))
  }.toList

  def getNumCycles: Int = inTransaction {
    cycles.size + 1
  }

  def getCurrentCycleIndex = inTransaction{
    cycles.size - 1
  }

  def getCycleWithIndex(cycIndex: Int): Option[Cycle] = inTransaction{
    cycles.drop(cycIndex).headOption
  }

  def firstCycle: Option[Cycle] = Some(Cycle(getFirstDay.map(_.id).getOrElse(new DateTime), "unnamed first cycle".getBytes))
  def lastCycle: Option[Cycle] = inTransaction{from(cycles)(c => select(c) orderBy(c.id desc)).headOption}
  /** @return The next cycle with id greater (not equal) than given date. */
  def nextCycle(date: Date): Option[Cycle] = inTransaction{from(cycles)(c => where(c.id gt date) select(c) orderBy(c.id asc)).headOption}
  /** @return The next cycle with id less (not equal) than given date. */
  def prevCycle(date: Date): Option[Cycle] = inTransaction{from(cycles)(c => where(c.id lt date) select(c) orderBy(c.id desc)).headOption}

  def currentCycle: Cycle = getCycleWithIndex(getCurrentCycleIndex).getOrElse(firstCycle.get /*always defined*/)

  def cycleForDate(date: Date) = inTransaction {
    from(cycles)(c => where(c.id lt date) select(c) orderBy(c.id desc))
  }.headOption.getOrElse(firstCycle.get /*always defined*/)

  def cycleIndexOf(cycle: Cycle): Int = inTransaction {
    cycles.toList.indexOf(cycle) + 1
  }

  def addCycle(cycle: Cycle) {
    inTransaction {
      val oldValueOption = from(cycles)(c => where(c.id === cycle.id) select c).headOption
      oldValueOption match {
        case Some(old) => {
          cycles.update(cycle)
          modifications.publish(CycleModifiedEvent(cycle,old))
        }
        case None => {
          cycles.insert(cycle)
          modifications.publish(CycleCreatedEvent(cycle))
        }
      }
    }
  }

  def removeCycle(date: Date) {
    inTransaction {
      from(cycles)(c => where(c.id === date) select(c)).foreach{cycle =>
        cycles.delete(date)
        modifications.publish(CycleDeletedEvent(cycle))
      }
    }
  }

  def daysOfCycle(cycIndex: Int): Query[Day] = inTransaction {
    val beginDate: Date = getCycleWithIndex(cycIndex).map(_.id).getOrElse((new DateTime).minusYears(100))
    val endDate: Date = getCycleWithIndex(cycIndex + 1).map(_.id).getOrElse((new DateTime).plusYears(100))

    from(days)(d =>
      where((d.id gte beginDate) and (d.id lt endDate))
        select(d)
    )
  }

  def createOrUpdateDay(day: Day) {
    inTransaction {
      try {
        days.update(day)
      } catch {
        case x => days.insert(day)
      }
    }
    modifications.publish(DayModifiedEvent(day))
  }

  /** Do everything necessary after application start to use the database. Also check that the db layout is current.
    */
  private def createSession(): Session = {
    Session.create(
      java.sql.DriverManager.getConnection(dbUrl),
      new H2Adapter)
  }

  def initDB() {
    SessionFactory.concreteFactory = Some(() => createSession())

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

trait TableModifiedEvent[T] extends Event {
  def affectedTable: Table[T]
}
object TableModifiedEvent{
  def unapply[T](tme: TableModifiedEvent[T]) = Some(tme.affectedTable)
}
case class DayModifiedEvent(newValue: Day) extends TableModifiedEvent[Day] {
  def affectedTable: Table[Day] = DataBase.days
}

sealed trait CycleEvent extends TableModifiedEvent[Cycle] {
  def affectedTable: Table[Cycle] = DataBase.cycles
  def cycle: Cycle
}
case class CycleModifiedEvent(newCycle: Cycle, oldCycle: Cycle) extends CycleEvent{
  def cycle = oldCycle
}
case class CycleCreatedEvent(cycle: Cycle) extends CycleEvent
case class CycleDeletedEvent(cycle: Cycle) extends CycleEvent

