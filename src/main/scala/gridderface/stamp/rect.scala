package gridderface.stamp

import java.awt._
import java.awt.geom.{Rectangle2D, Ellipse2D}
// Too many small classes so I'm putting them together.

object FillRectStamp extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = g2d fill unitRect
}

class OutlineRectStamp(stroke: Stroke) extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = {
    g2d setStroke stroke
    g2d draw unitRect
  }
}

class BulbRectStamp(size: Double, xOffset: Double = 0, yOffset: Double = 0) extends ScalableRectStamp {
  val sx = 0.5 - size / 2.0
  val bulb = new Ellipse2D.Double(xOffset + sx, yOffset + sx, size, size)
  override def drawUnit(g2d: Graphics2D) = {
    g2d fill bulb
  }
}

class CircleRectStamp(size: Double, stroke: Stroke = Strokes.normalStroke) extends ScalableRectStamp {
  val sx = 0.5 - size / 2.0
  val circ = new Ellipse2D.Double(sx, sx, size, size)
  override def drawUnit(g2d: Graphics2D) = {
    g2d setStroke stroke
    g2d draw circ
  }
}
