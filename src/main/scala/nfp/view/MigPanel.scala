package nfp.view

import swing._
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Geier
 * Date: 05.10.11
 */

class MigPanel(layoutConstr: String = "", colConstr: String = "", rowConstr: String = "") extends Panel with LayoutContainer {
  override lazy val peer: JPanel = new JPanel(new MigLayout(layoutConstr, colConstr, rowConstr))

  type Constraints = String

  private def layoutManager = peer.getLayout.asInstanceOf[MigLayout]

  protected def constraintsFor(comp: Component) = layoutManager.getComponentConstraints(comp.peer).asInstanceOf[String]
  protected def areValid(c: Constraints): (Boolean, String) = (true, "")
  def add(c: Component, const: String = "") { peer.add(c.peer, const) }
}