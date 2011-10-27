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

package nfp

import java.sql.{Date => SQLDate}
import java.util.Date
import org.joda.time.{LocalDate, DateTime}

/** Some implicit conversion between JodaTime, java.util.Date and java.sql.Date.
  *
  * @see java.sql.Date
  * @see java.util.Date
  * @see org.joda.time.DateTime
  * @see org.joda.time.LocalDate
  *
  * @author Thomas Geier
  */
object DateConversion {
  implicit def joda2sql(d: DateTime): SQLDate = new SQLDate(d.getMillis)
  implicit def joda2java(d: DateTime): Date = new Date(d.getMillis)

  implicit def sql2joda(d: SQLDate): DateTime = new DateTime(d.getTime)
  implicit def sql2java(d: SQLDate): Date = new Date(d.getTime)

  implicit def java2joda(d: Date): DateTime = new DateTime(d.getTime)
  implicit def java2sql(d: Date): SQLDate = new SQLDate(d.getTime)

  implicit def dateTime2SQLDate(dt: LocalDate): java.sql.Date = new java.sql.Date(dt.toDateMidnight.getMillis)
  implicit def sqlDate2DateTime(dt: java.sql.Date): LocalDate = new LocalDate(dt.getTime)
}