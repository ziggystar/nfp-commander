package nfp.model

import org.squeryl.Schema
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._

object DataBase extends Schema {
  val days = table[Day]
  val properties = table[KeyValue]

  def putProperty(key: String, value: String) = transaction{properties.insertOrUpdate(new KeyValue(key, value))}
  def getProperty(key: String): Option[String] = transaction{
    from(properties)(p => where(p.id === key) select p.value).headOption
  }
}


