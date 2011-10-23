package nfp.view

import swing._
import event.Event
import nfp.model.Day
import com.toedter.calendar.JDateChooser
import org.joda.time.DateMidnight
import java.util.Date

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 25.06.11
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */

class DayEditorPanel extends MigPanel {
  import nfp.DateConversion._

  val dateChooser: JDateChooser = new JDateChooser(new Date)
  this.add(new Label("Tag"))
  this.add(Component.wrap(dateChooser), "w 150, wrap")


  val tfTemp: TextFieldFloatOption = new TextFieldFloatOption
  this.add(new Label("Temperatur"))
  this.add(tfTemp, "w 150, wrap")

  val cbSchleim: ComboBox[String] = new ComboBox[String](Seq("-","kein", "t", "f", "S", "(S)", "S+", "(S+)"))
  this.add(new Label("Schleim"))
  this.add(cbSchleim, "w 150, wrap")

  val cbMumuPos: ComboBox[String] = new ComboBox[String](Seq("-","hoch", "mittel", "tief"))
  this.add(new Label("Muttermund Position"))
  this.add(cbMumuPos, "w 150, wrap")

  val cbMumuOpen: ComboBox[String] = new ComboBox[String](Seq("-","offen", "ka", "geschlossen"))
  this.add(new Label("Muttermund Öffnung"))
  this.add(cbMumuOpen, "w 150, wrap")

  val cbMumuSoft: ComboBox[String] = new ComboBox[String](Seq("-","weich", "solala", "hart"))
  this.add(new Label("Muttermund Härte"))
  this.add(cbMumuSoft,"w 150, wrap")

  val buttonUndo: Button = new Button("Undo")
  val buttonSave: Button = new Button("Speichern")
  this.add(buttonUndo)
  this.add(buttonSave)

  listenTo(buttonSave)
  listenTo(buttonUndo)
  reactions += {
    case swing.event.ActionEvent(c) if (c == buttonSave) => getDay.foreach(day => publish(DayModifiedEvent(day)))
  }

  def extractOComboBox(cb: ComboBox[String]): Option[String] = cb.item match {
    case "-" => None
    case x => Some(x)
  }

  def getDay: Option[Day] = for(
    date <- Option(dateChooser.getDate);
    temperature = tfTemp.getValue.map(_.toFloat);
    schleim = extractOComboBox(cbSchleim);
    mumuPos = extractOComboBox(cbMumuPos);
    mumuOpen = extractOComboBox(cbMumuOpen);
    mumuS = extractOComboBox(cbMumuSoft)
  ) yield new Day(date, temperature, None, schleim, mumuPos, mumuOpen, mumuS, Array.empty[Byte], false, None, None)

  def setContent(day: Day) {
    dateChooser.setDate(day.id)
    tfTemp.setValue(day.temperature)
    cbSchleim.selection.item = day.schleim.getOrElse("-")
    cbMumuPos.selection.item = day.mumuPosition.getOrElse("-")
    cbMumuOpen.selection.item = day.mumuOpen.getOrElse("-")
    cbMumuSoft.selection.item = day.mumuFest.getOrElse("-")
  }
}

case class DayModifiedEvent(newValue: Day) extends Event

