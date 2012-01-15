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

package nfp.model

/** Type class for table row objects that have a total order (like the date indexed Day and Cycle objects). */
trait OrderedEntity[A] {
  /**@return the first object in the order. None if there are no objects. */
  def first: Option[A]

  /**@return The last object in the order. None if there are no objects. */
  def last: Option[A]

  /**@return The previous object. None if this is the first one. */
  def prev(a: A): Option[A]

  /**@return The next object. None if this is the last one. */
  def next(a: A): Option[A]
}

object OrderedEntity {
  def oe[T: OrderedEntity]: OrderedEntity[T] = implicitly[OrderedEntity[T]]

  implicit object cycleOrdered extends OrderedEntity[Cycle] {

    def first: Option[Cycle] = DataBase.firstCycle
    def last: Option[Cycle] = DataBase.lastCycle

    def next(a: Cycle): Option[Cycle] = DataBase.nextCycle(a.id)
    def prev(a: Cycle): Option[Cycle] = DataBase.prevCycle(a.id)
  }
}