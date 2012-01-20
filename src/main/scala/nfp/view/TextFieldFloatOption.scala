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

package nfp.view

import swing._
import java.text.NumberFormat
import javax.swing.text.NumberFormatter
import java.util.Locale
import scala.util.control.Exception._

/**
  * A text field to enter a number. This should be replaced/removed.
  *
  * @author Thomas Geier
  */
class TextFieldFloatOption extends FormattedTextField(NumberFormat.getNumberInstance(Locale.GERMANY)) with Editor {

  type TData = Option[Float]

  private val formatter = peer.getFormatter.asInstanceOf[NumberFormatter]
  private val numberFormat = NumberFormat.getInstance(Locale.GERMANY)
  numberFormat.setMaximumFractionDigits(3)
  numberFormat.setParseIntegerOnly(false)
  numberFormat.setGroupingUsed(false)

  formatter.setFormat(numberFormat)

  def setValue(v: TextFieldFloatOption#TData) {
    text = v.map(v => "%.2f" format v).getOrElse("")
  }

  def getValue: TextFieldFloatOption#TData = text match {
    case "-" => None
    case "" => None
    case t => allCatch.opt(numberFormat.parse(t).floatValue())
  }
}

trait Editor extends Component {
  type TData
  def setValue(v: TData): Unit
  def getValue: TData
}





