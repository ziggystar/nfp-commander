package nfp.view

import swing._
import java.text.NumberFormat
import javax.swing.text.NumberFormatter
import java.util.Locale

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 25.06.11
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */

class TextFieldFloatOption extends FormattedTextField(NumberFormat.getNumberInstance(Locale.GERMANY)) with Editor {

  type TData = Option[Float]

  private val formatter = peer.getFormatter.asInstanceOf[NumberFormatter]
  private val numberFormat = NumberFormat.getInstance(Locale.GERMANY)
  numberFormat.setMaximumFractionDigits(3)
  numberFormat.setParseIntegerOnly(false)
  numberFormat.setGroupingUsed(false)

  formatter.setFormat(numberFormat)
//  formatter.setMaximum(44)
//  formatter.setMinimum(30)

  def setValue(v: TextFieldFloatOption#TData) {
    text = v.map(v => "%.2f" format v).getOrElse("")
  }

  def getValue: TextFieldFloatOption#TData = try {
    Some(text.replace(',','.').toFloat) filter (t => t > 32f && t < 44f)
  } catch {
    case _ => None
  }
}





