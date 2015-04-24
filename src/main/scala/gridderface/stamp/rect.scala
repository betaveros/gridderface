package gridderface.stamp

import java.awt._
import java.awt.geom.{Rectangle2D, Ellipse2D}
// Too many small classes so I'm putting them together.

case class FullRectStamp(dv: DrawVal = Fill) extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = dv.draw(g2d, unitRect)
}

case class CircleRectStamp(size: Double, dv: DrawVal = Draw(NormalStrokeVal), xOffset: Double = 0, yOffset: Double = 0) extends ScalableRectStamp {
  val sx = 0.5 - size / 2.0
  val circ = new Ellipse2D.Double(xOffset + sx, yOffset + sx, size, size)
  override def drawUnit(g2d: Graphics2D) = {
    dv.draw(g2d, circ)
  }
}
