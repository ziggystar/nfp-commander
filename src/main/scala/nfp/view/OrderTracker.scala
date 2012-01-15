/*
 * Copyright (C) 2012 thomas
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

import nfp.model.OrderedEntity
import swing.event.Event
import swing.{Action, Button, Publisher, Reactor}

/** Keeps track of an object that's part of a total order. */
class OrderTracker[T: OrderedEntity](private var value: T) extends Reactor with Publisher {

  import OrderedEntity._

  case class EntityChangedEvent(newValue: T) extends Event

  def get: T = value

  def set(newVal: T) {
    if (newVal != value) {
      value = newVal
      publish(EntityChangedEvent(value))
    }
  }

  def skipNext() {
    oe[T].next(value).foreach(this.set)
  }

  def skipPrev() {
    oe[T].prev(value).foreach(this.set)
  }

  def createSkipNextButton(text: String): Button = {
    val button = new Button(Action(text)(skipNext()))
    button.enabled = oe[T].next(value).isDefined
    button.listenTo(this)
    button.reactions += {
      case EntityChangedEvent(_) => {button.enabled = oe[T].next(value).isDefined}
    }

    button
  }
  def createSkipPrevButton(text: String) = {
    val button = new Button(Action(text)(skipPrev()))
    button.enabled = oe[T].prev(value).isDefined
    button.listenTo(this)
    button.reactions += {
      case EntityChangedEvent(_) => {button.enabled = oe[T].prev(value).isDefined}
    }

    button
  }
}