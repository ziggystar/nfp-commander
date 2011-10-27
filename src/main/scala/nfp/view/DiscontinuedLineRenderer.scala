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

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer

/**
  * The custom graph renderer used for the NFP chart. Extra functionality over XYLineAndShapeRenderer is
  *  - draw some shapes not filled, determined by a predicate function on the series index
  *  - don't draw certain connections based on a predicate on the series index.
  *
  * @author Thomas Geier
  * Date: 24.10.11
  */
class DiscontinuedLineRenderer extends XYLineAndShapeRenderer {

  /**
    * If this returns false for a series index, then the line to the left of the point is nor drawn.
    */
  var renderLinePredicate: Int => Boolean = null

  //todo: change this so the corresponding shape gets filled with white or a custom color
  /**
    * If this function returns false for a series index, then the corresponding shape will not be filled.
    */
  var fillItemShape: Int => Boolean = null

  override def getItemLineVisible(series: Int, item: Int): Boolean = (renderLinePredicate == null) || renderLinePredicate(item)

  override def getItemShapeFilled(series: Int, item: Int): Boolean = fillItemShape == null || fillItemShape(item)
}