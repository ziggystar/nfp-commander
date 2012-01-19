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

import org.jfree.chart.axis.ValueAxis
import org.jfree.data.xy.XYDataset
import org.jfree.chart.entity.EntityCollection
import scala.{Double, Unit, Int}
import org.jfree.chart.plot._
import org.jfree.util.ShapeUtilities
import org.jfree.chart.renderer.xy.{XYShapeRenderer, XYItemRendererState}
import java.awt.geom.{Line2D, Rectangle2D}
import org.jfree.data.general.DatasetUtilities
import org.jfree.data.{Range => JFRange}
import java.awt._
import image.BufferedImage
import javax.imageio.ImageIO
import collection.Seq
import java.net.URL

class XYCategoryRenderer(val constantValue: Double) extends XYShapeRenderer {
  val iconFiles = IndexedSeq("blood1.png","blood2.png","blood3.png")
  val icons: Seq[BufferedImage] = iconFiles.map{file =>
    val resource: URL = this.getClass.getResource(file)
    ImageIO.read(resource)
  }

  /**
   * Overriden because of NPE for empty Datasets in JFC.
   *
   * Returns the lower and upper bounds (range) of the x-values in the
   * specified dataset.
   *
   * @param dataset  the dataset (<code>null</code> permitted).
   *
   * @return The range (<code>null</code> if the dataset is <code>null</code>
   *         or empty).
   */
  override def findDomainBounds(dataset: XYDataset): JFRange = new JFRange(constantValue, constantValue)

  /**
   * Returns the range of values the renderer requires to display all the
   * items from the specified dataset.
   *
   * @param dataset  the dataset (<code>null</code> permitted).
   *
   * @return The range (<code>null</code> if the dataset is <code>null</code>
   *         or empty).
   */
  override def findRangeBounds(dataset: XYDataset): JFRange = (for(
    ds <- Option(dataset);
    r <- Option(DatasetUtilities.findRangeBounds(ds, false)))
  yield new JFRange(r.getLowerBound, r.getUpperBound)).orNull


  /**
   * Draws the block representing the specified item.
   *
   * @param g2  the graphics device.
   * @param state  the state.
   * @param dataArea  the data area.
   * @param info  the plot rendering info.
   * @param plot  the plot.
   * @param domainAxis  the x-axis.
   * @param rangeAxis  the y-axis.
   * @param dataset  the dataset.
   * @param series  the series index.
   * @param item  the item index.
   * @param crosshairState  the crosshair state.
   * @param pass  the pass index.
   */
  override def drawItem(g2: Graphics2D,
               state: XYItemRendererState,
               dataArea: Rectangle2D,
               info: PlotRenderingInfo,
               plot: XYPlot,
               domainAxis: ValueAxis,
               rangeAxis: ValueAxis,
               dataset: XYDataset,
               series: Int,
               item: Int,
               crosshairState: CrosshairState,
               pass: Int) {
    var hotspot: Shape = null
    var entities: EntityCollection = null
    if (info != null) {
      entities = info.getOwner.getEntityCollection
    }
    val x: Double = dataset.getXValue(series, item)
    val y: Double = dataset.getYValue(series, item)

    if (x.isNaN || y.isNaN) {
      return
    }

    val iconNumber = y match {
      case 1d => 0
      case 2d => 1
      case 3d => 2
      case _ => return
    }
    val icon = icons(iconNumber)

    //TODO cache this in a member
    val iconWidth = 20

    val transX: Double = domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge)
    val transY: Double = rangeAxis.valueToJava2D(constantValue, dataArea, plot.getRangeAxisEdge)
    val orientation: PlotOrientation = plot.getOrientation
//    if ((pass == 0) && this.guideLinesVisible) {
//      g2.setStroke(this.guideLineStroke)
//      g2.setPaint(this.guideLinePaint)
//      if (orientation eq PlotOrientation.HORIZONTAL) {
//        g2.draw(new Line2D.Double(transY, dataArea.getMinY, transY, dataArea.getMaxY))
//        g2.draw(new Line2D.Double(dataArea.getMinX, transX, dataArea.getMaxX, transX))
//      }
//      else {
//        g2.draw(new Line2D.Double(transX, dataArea.getMinY, transX, dataArea.getMaxY))
//        g2.draw(new Line2D.Double(dataArea.getMinX, transY, dataArea.getMaxX, transY))
//      }
//    }
//    else
    if (pass == 1) {
      var shape: Shape = new Rectangle(0,0,50,50)
      if (orientation eq PlotOrientation.HORIZONTAL) {
        shape = ShapeUtilities.createTranslatedShape(shape, transY, transX)
      }
      else if (orientation eq PlotOrientation.VERTICAL) {
        shape = ShapeUtilities.createTranslatedShape(shape, transX, transY)
      }
      hotspot = shape
      if (shape.intersects(dataArea)) {
        g2.drawImage(icon,transX.toInt - iconWidth/2,transY.toInt - iconWidth/2,transX.toInt+iconWidth/2,transY.toInt+iconWidth/2,0,0,50,50,null)
        //g2.fill(shape)
//        if (this.drawOutlines) {
//          if (getUseOutlinePaint) {
//            g2.setPaint(getItemOutlinePaint(series, item))
//          }
//          else {
//            g2.setPaint(getItemPaint(series, item))
//          }
//          g2.setStroke(getItemOutlineStroke(series, item))
//          g2.draw(shape)
//        }
      }
      if (entities != null) {
        addEntity(entities, hotspot, dataset, series, item, transX, transY)
      }
    }
  }
}