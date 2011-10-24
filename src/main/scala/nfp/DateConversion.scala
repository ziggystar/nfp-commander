package nfp

import java.sql.{Date => SQLDate}
import java.util.Date
import org.joda.time.{LocalDate, DateTime}

/**
 * Some implicit conversion.
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