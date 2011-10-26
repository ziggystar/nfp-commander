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

import org.jfree.data.xy.XYDataset
import org.jfree.chart.axis.ValueAxis
import java.awt.geom.Rectangle2D
import org.jfree.chart.plot.{CrosshairState, XYPlot}
import org.jfree.chart.entity.EntityCollection
import org.jfree.chart.renderer.xy.{XYItemRendererState, XYLineAndShapeRenderer}
import org.jfree.data.time.{TimeSeriesDataItem, TimeSeriesCollection, TimeSeries}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer.State
import java.awt.{Paint, Shape, Graphics2D}

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 24.10.11
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */

class DiscontinuedLineRenderer extends XYLineAndShapeRenderer {
  var renderLinePredicate: Int => Boolean = null
  var fillItemShape: Int => Boolean = null


  override def getItemLineVisible(series: Int, item: Int): Boolean = (renderLinePredicate == null) || renderLinePredicate(item)

  override def getItemShapeFilled(series: Int, item: Int): Boolean = fillItemShape == null || fillItemShape(item)
}