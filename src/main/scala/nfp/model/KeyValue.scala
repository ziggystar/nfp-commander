package nfp.model

import org.squeryl.KeyedEntity

class KeyValue(val id: String,
               val value: String) extends KeyedEntity[String] {
  def this() = this("","")
}