package gridderface.stamp

import java.awt.{ Graphics2D }
import java.awt.geom.{ Path2D, Rectangle2D, Ellipse2D }
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

case class DownPointingFullRectStamp(dv: DrawVal = Draw(NormalStrokeVal)) extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = dv.draw(g2d, DownPointingFullRectStamp.shape)
}
object DownPointingFullRectStamp {
  val shape = new Path2D.Float
  shape.moveTo(0, 0)
  for ((x,y) <- List((1.0, 0.0), (1.0, 1.0), (0.5, 1.25), (0.0, 1.0)))
    shape.lineTo(x, y)
  shape.closePath
}

case class RightPointingFullRectStamp(dv: DrawVal = Draw(NormalStrokeVal)) extends ScalableRectStamp {
  override def drawUnit(g2d: Graphics2D) = dv.draw(g2d, RightPointingFullRectStamp.shape)
}
object RightPointingFullRectStamp {
  val shape = new Path2D.Float
  shape.moveTo(0, 0)
  for ((x,y) <- List((1.0, 0.0), (1.25, 0.5), (1.0, 1.0), (0.0, 1.0)))
    shape.lineTo(x, y)
  shape.closePath
}
