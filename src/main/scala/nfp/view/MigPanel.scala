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
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

/** Scala wrapper for MigLayout.
  *
  * @author Thomas Geier
  */
class MigPanel(layoutConstraints: String = "", columnConstraints: String = "", rowConstraints: String = "") extends Panel with LayoutContainer {
  override lazy val peer: JPanel = new JPanel(new MigLayout(layoutConstraints, columnConstraints, rowConstraints))

  type Constraints = String

  private def layoutManager = peer.getLayout.asInstanceOf[MigLayout]

  protected def constraintsFor(comp: Component) = layoutManager.getComponentConstraints(comp.peer).asInstanceOf[String]

  protected def areValid(c: Constraints): (Boolean, String) = (true, "")

  def add(c: Component, const: String = "") {
    peer.add(c.peer, const)
  }
}