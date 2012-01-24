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
import com.toedter.calendar.JDateChooser
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import nfp.model.{DataBase, Day}
import nfp.view.I18n._

/**
  * GUI element to display and modify a Day object.
  * @see nfp.model.Day
  *
  * @author Thomas Geier
  * Date: 25.06.11
  */
class DayEditorPanel(initialDay: Day) extends MigPanel {

  import nfp.DateConversion._

  case class StringOption(label: String,
                          values: Seq[String],
                          extractor: Day => Option[String],
                          modifier: Option[String] => Day => Day,
                          default: Option[String] = None)

  val stringOptions = Seq.empty[StringOption] :+
    StringOption(
      "Schleim",
      Seq("kein", "t", "f", "S", "(S)", "S+", "(S+)"),
      _.schleim,
      v => _.copy(schleim = v),
      initialDay.schleim) :+
    StringOption(
      "Muttermund Position",
      Seq("hoch", "mittel", "tief"),
      _.mumuPosition,
      v => _.copy(mumuPosition = v),
      initialDay.mumuPosition) :+
    StringOption(
      "Muttermund Öffnung",
      Seq("offen", "mittel", "geschlossen"),
      _.mumuOpen,
      v => _.copy(mumuOpen = v),
      initialDay.mumuOpen
    ) :+
    StringOption(
      "Muttermund Härte",
      Seq("weich", "solala", "hart"),
      _.mumuFest,
      v => _.copy(mumuFest = v),
      initialDay.mumuFest
    ) :+
    StringOption(
      "Blutung",
      Seq("keine", "leicht", "mittel", "stark", "Schmier"),
      _.blutung,
      v => _.copy(blutung = v),
      initialDay.blutung
    ) :+
    StringOption(
      'sex,
      Seq('none, 'contraception, 'no_contraception),
      _.sex,
      v => _.copy(sex = v),
      initialDay.sex
    )

  private val dateChooser: JDateChooser = new JDateChooser(initialDay.id)
  this.add(new Label('day))
  this.add(Component.wrap(dateChooser), "w 150, wrap")

  val tfTemp: TextFieldFloatOption = new TextFieldFloatOption
  tfTemp.setValue(initialDay.temperature)
  this.add(new Label('temperature))
  private val cbKlammer: CheckBox = new CheckBox
  cbKlammer.selected = !initialDay.ausklammern
  this.add(tfTemp, "split 2, grow")
  this.add(cbKlammer, "wrap")

  val soComboBoxes = stringOptions.map(so => so -> new ComboBox[String]("-" +: so.values))
  soComboBoxes.foreach {
    case (so, cb) =>
      so.default.foreach {
        cb.selection.item = _
      }
      this.add(new Label(so.label))
      this.add(cb, "w 150, wrap")
  }

  val dateChooserListener = new PropertyChangeListener {
    def propertyChange(p1: PropertyChangeEvent) {
      if(p1.getSource == dateChooser){
        DataBase.getDayAtDate(dateChooser.getDate).foreach(setContent(_))
      }
    }
  }
  dateChooser.addPropertyChangeListener(dateChooserListener)

  def extractOComboBox(cb: ComboBox[String]): Option[String] = cb.item match {
    case "-" => None
    case "" => None
    case x => Some(x)
  }

  def getDay: Option[Day] = {
    val unfinishedResult: Option[Day] = for (
      date <- Option(dateChooser.getDate);
      temperature = tfTemp.getValue.map(_.toFloat);
      klammern = !cbKlammer.selected
    ) yield new Day(date, temperature, None, None, None, None, None, Array.empty[Byte], klammern, None, None)
    val cbValues = soComboBoxes.map(_._2).map(extractOComboBox)
    stringOptions.map(_.modifier).zip(cbValues).foldLeft(unfinishedResult) {
      case (dayOpt, (mod, v)) => dayOpt.map(mod(v))
    }
  }

  def setContent(day: Day) {
    dateChooser.removePropertyChangeListener(dateChooserListener)
    dateChooser.setDate(day.id)
    dateChooser.addPropertyChangeListener(dateChooserListener)

    tfTemp.setValue(day.temperature)
    soComboBoxes.foreach {
      case (so, cb) =>
        cb.selection.item = so.extractor(day).getOrElse("-")
    }
    cbKlammer.selected = !day.ausklammern
  }
}

