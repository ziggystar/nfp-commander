package nfp

import org.joda.time.DateTime
import java.sql.{Date => SQLDate}
import java.util.Date

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
}