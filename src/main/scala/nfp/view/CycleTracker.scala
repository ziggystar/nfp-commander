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

import swing.{Publisher, Reactor}
import swing.event.Event
import nfp.model._

/**Tracks a cycle and notifies if the cycle or its end date (which is defined by the start date of the next cycle) changes.
 * If the cycle gets deleted it jumps to the next cycle.
 * @param myCycle Cycle to start with.
 */
class CycleTracker(myCycle: Cycle) extends OrderTracker[Cycle](myCycle) {

  this.listenTo(DataBase.modifications)
  this.reactions += {
    case CycleDeletedEvent(dc) if (dc == get) => set(DataBase.cycleForDate(get.id))
    case CycleModifiedEvent(newCycle, oldCycle) if (oldCycle == get) => set(newCycle)
    //TODO could be more specific
    case ce: CycleEvent => publish(EntityChangedEvent(get))
  }
}

//class DayTracker(myDay: Day) extends OrderTracker[Day](myDay) {
//  this.listenTo(DataBase.modifications)
//  this.reactions += {
//    case DayModifiedEvent(d) if (d.id == get.id) => set(d)
//  }
//}


