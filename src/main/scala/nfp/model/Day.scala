package nfp.model

import java.sql.Date
import org.squeryl.KeyedEntity

class Day(val id: Date,
          val temperature: Option[Float],
          val measureTime: Option[String],
          val schleim: Option[String],
          val mumuPosition: Option[String],
          val mumuOpen: Option[String],
          val mumuFest: Option[String],
          val comment: Array[Byte],
          val ausklammern: Boolean,
          val blutung: Option[String], //"schmier", "schwach", "mittel, "stark"
          val sex: Option[String]
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

  override def toString = "Day(%s,%s,%s,%s)".format(id,temperature,schleim, mumuPosition, mumuOpen, comment)
}


