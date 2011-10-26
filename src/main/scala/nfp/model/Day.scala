package nfp.model

import java.sql.Date
import org.squeryl.KeyedEntity

case class Day(id: Date,
          temperature: Option[Float],
          measureTime: Option[String],
          schleim: Option[String],
          mumuPosition: Option[String],
          mumuOpen: Option[String],
          mumuFest: Option[String],
          comment: Array[Byte],
          ausklammern: Boolean,
          blutung: Option[String], //"schmier", "schwach", "mittel, "stark"
          sex: Option[String]
           ) extends KeyedEntity[Date] {
  def this() = this(
    new java.sql.Date(0),
    Some(0f),
    Some(""),
    Some(""),
    Some(""),
    Some(""),
    Some(""),
    Array.empty[Byte],
    false,
    Some(""),
    Some(""))
}


