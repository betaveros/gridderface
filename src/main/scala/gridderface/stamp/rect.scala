package gridderface.stamp

import java.awt._
import java.awt.geom.{Rectangle2D, Ellipse2D}
// Too many small classes so I'm putting them together.

case object FillRectStamp extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = g2d fill unitRect
}

case class OutlineRectStamp(sv: StrokeVal) extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = {
    g2d setStroke sv.stroke
    g2d draw unitRect
  }
}

case class CircleRectStamp(size: Double, dv: DrawVal = Draw(NormalStrokeVal), xOffset: Double = 0, yOffset: Double = 0) extends ScalableRectStamp {
  val sx = 0.5 - size / 2.0
  val circ = new Ellipse2D.Double(xOffset + sx, yOffset + sx, size, size)
  override def drawUnit(g2d: Graphics2D) = {
    dv.draw(g2d, circ)
  }
}
