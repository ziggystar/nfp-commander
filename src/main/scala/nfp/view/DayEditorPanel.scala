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
import event.Event
import nfp.model.Day
import java.util.Date
import javax.swing.BorderFactory
import com.toedter.calendar.JDateChooser

/**
 * GUI element to display and modify a Day object.
 * @see nfp.model.Day
 *
 * @author Thomas Geier
 * Date: 25.06.11
 */
class DayEditorPanel extends MigPanel {
  import nfp.DateConversion._

  case class StringOption(label: String, values: Seq[String], extractor: Day => Option[String], modifier: Option[String] => Day => Day, default: Option[String] = None)
  val stringOptions = Seq.empty[StringOption] :+
    StringOption(
      "Schleim",
      Seq("kein", "t", "f", "S", "(S)", "S+", "(S+)"),
      _.schleim,
      v => _.copy(schleim = v)) :+
    StringOption(
      "Muttermund Position",
      Seq("hoch", "mittel", "tief"),
      _.mumuPosition,
      v => _.copy(mumuPosition = v)) :+
    StringOption(
      "Muttermund Öffnung",
      Seq("offen", "mittel", "geschlossen"),
      _.mumuOpen,
      v => _.copy(mumuOpen = v)
    ) :+
    StringOption(
      "Muttermund Härte",
      Seq("weich", "solala", "hart"),
      _.mumuFest,
      v => _.copy(mumuFest = v)
    ) :+
    StringOption(
      "Blutung",
      Seq("keine", "leicht", "mittel", "stark", "Schmier"),
      _.blutung,
      v => _.copy(blutung = v),
      Some("keine")
    ) :+
    StringOption(
      "Sex",
      Seq("keiner", "verhütet", "unverhütet"),
      _.sex,
      v => _.copy(sex = v),
      Some("keiner")
    )

  this.peer.setBorder(BorderFactory.createTitledBorder("Eintragen/Ändern"))

  val dateChooser: JDateChooser = new JDateChooser(new Date)
  this.add(new Label("Tag"))
  this.add(Component.wrap(dateChooser), "w 150, wrap")

  val tfTemp: TextFieldFloatOption = new TextFieldFloatOption
  this.add(new Label("Temperatur"))
  val cbKlammer: CheckBox = new CheckBox
  cbKlammer.selected = true
  this.add(tfTemp, "split 2, grow")
  this.add(cbKlammer, "wrap")

  val soComboBoxes = stringOptions.map(so => so -> new ComboBox[String]("-" +: so.values))
  soComboBoxes.foreach{ case (so, cb) =>
    so.default.foreach{cb.selection.item = _}
    this.add(new Label(so.label))
    this.add(cb, "w 150, wrap")
  }

  val buttonSave: Button = new Button("Speichern")
  this.add(buttonSave, "span 2, gap push")

  listenTo(buttonSave)
  reactions += {
    case swing.event.ActionEvent(c) if (c == buttonSave) => getDay.foreach(day => publish(DayModifiedEvent(day)))
  }

  def extractOComboBox(cb: ComboBox[String]): Option[String] = cb.item match {
    case "-" => None
    case x => Some(x)
  }

  def getDay: Option[Day] = {
    val unfinishedResult: Option[Day] = for(
      date <- Option(dateChooser.getDate);
      temperature = tfTemp.getValue.map(_.toFloat);
      klammern = !cbKlammer.selected
    ) yield new Day(date, temperature, None, None, None, None, None, Array.empty[Byte], klammern, None, None)
    val cbValues = soComboBoxes.map(_._2).map(extractOComboBox)
    stringOptions.map(_.modifier).zip(cbValues).foldLeft(unfinishedResult){case (dayOpt,(mod,v)) => dayOpt.map(mod(v))}
  }

  def setContent(day: Day) {
    dateChooser.setDate(day.id)
    tfTemp.setValue(day.temperature)
    soComboBoxes.foreach{case (so, cb) =>
      cb.selection.item = so.extractor(day).getOrElse("-")
    }
    cbKlammer.selected = !day.ausklammern
  }
}

case class DayModifiedEvent(newValue: Day) extends Event

