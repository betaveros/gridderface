package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.geom.Path2D

class InequalityLineStamp(stroke: Stroke, isLess: Boolean) extends ScalableLineStamp {
  val path = new Path2D.Double()
  val sign = if (isLess) 1 else -1
  path.moveTo(0.25, sign * 0.125)
  path.lineTo(0.50, sign * -0.125)
  path.lineTo(0.75, sign * 0.125)
  override def isFlipped(x0: Double, y0: Double, x1: Double, y1: Double): Boolean = { x1 - x0 < y1 - y0 }
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke stroke
    g2d draw path
  }
}
