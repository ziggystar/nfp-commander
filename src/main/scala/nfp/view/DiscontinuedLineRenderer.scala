package nfp.view

import org.jfree.data.xy.XYDataset
import org.jfree.chart.axis.ValueAxis
import java.awt.geom.Rectangle2D
import org.jfree.chart.plot.{CrosshairState, XYPlot}
import org.jfree.chart.entity.EntityCollection
import org.jfree.chart.renderer.xy.{XYItemRendererState, XYLineAndShapeRenderer}
import org.jfree.data.time.{TimeSeriesDataItem, TimeSeriesCollection, TimeSeries}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer.State
import java.awt.{Shape, Graphics2D}

/**
 * Created by IntelliJ IDEA.
 * User: thomas
 * Date: 24.10.11
 * Time: 21:49
 * To change this template use File | Settings | File Templates.
 */

class DiscontinuedLineRenderer extends XYLineAndShapeRenderer {
  var renderLinePredicate: Int => Boolean = null

  private var suppressLineDrawing = false
  override def drawPrimaryLine(state: XYItemRendererState,
                                     g2: Graphics2D,
                                     plot: XYPlot,
                                     dataset: XYDataset,
                                     pass: Int,
                                     series: Int,
                                     item: Int,
                                     domainAxis: ValueAxis,
                                     rangeAxis: ValueAxis,
                                     dataArea: Rectangle2D) {
    val dataItem: TimeSeriesDataItem = dataset.asInstanceOf[TimeSeriesCollection].getSeries(series).getDataItem(item)
    if(renderLinePredicate != null && !renderLinePredicate(item)){
      //remove the last drawn segment
      suppressLineDrawing = true
    }

    super.drawPrimaryLine(state,g2,plot,dataset,pass,series,item,domainAxis,rangeAxis,dataArea)

    suppressLineDrawing = false
  }

  override def drawFirstPassShape(g2: Graphics2D, pass: Int, series: Int, item: Int, shape: Shape) {
    if(!suppressLineDrawing)
      super.drawFirstPassShape(g2,pass,series,item,shape)
  }
}