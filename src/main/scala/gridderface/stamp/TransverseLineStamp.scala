package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.geom.Line2D

class TransverseLineStamp(stroke: Stroke) extends ScalableLineStamp {
  val edge = new Line2D.Double(0.5, -0.5, 0.5, 0.5)
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke stroke
    g2d draw edge
  }
}
