package gridderface.stamp

import java.awt._
import java.awt.geom._

case class RectangleArcRectStamp(size: Double, xOffset: Double = 0, yOffset: Double = 0, dv: DrawVal, topRightArc: Boolean, bottomRightArc: Boolean, bottomLeftArc: Boolean, topLeftArc: Boolean) extends ScalableRectStamp {
  val path = {
    val sx = 0.5 - size / 2.0
    if (topRightArc && bottomRightArc && bottomLeftArc && topLeftArc)
      new Ellipse2D.Double(xOffset + sx, yOffset + sx, size, size)
    else if (!topRightArc && !bottomRightArc && !bottomLeftArc && !topLeftArc)
      new Rectangle2D.Double(xOffset + sx, yOffset + sx, size, size)
    else {
      val p = RectangleArcRectStamp.buildRectangleArcAtOrigin(topRightArc, bottomRightArc, bottomLeftArc, topLeftArc)

      p.transform(AffineTransform.getScaleInstance(size / 2.0, size / 2.0))
      p.transform(AffineTransform.getTranslateInstance(0.5, 0.5))
      p
    }
  }
  override def drawUnit(g2d: Graphics2D) = {
    dv.draw(g2d, path)
  }
}

object RectangleArcRectStamp {
  val magic = 0.552 // for approximating a circle with bezier curves
  def shift(p: Path2D, x1: Double, y1: Double, x2: Double, y2: Double, isArc: Boolean) {
    if (isArc) {
      p.curveTo(
        x1 + magic * x2, y1 + magic * y2,
        magic * x1 + x2, magic * y1 + y2,
        x2, y2)
    } else {
      p.lineTo(x1 + x2, y1 + y2); p.lineTo(x2, y2)
    }
  }
  def buildRectangleArcAtOrigin(topRightArc: Boolean, bottomRightArc: Boolean, bottomLeftArc: Boolean, topLeftArc: Boolean) = {
    val p = new Path2D.Double
    p.moveTo(0, -1)
    shift(p,  0, -1,  1,  0, topRightArc)
    shift(p,  1,  0,  0,  1, bottomRightArc)
    shift(p,  0,  1, -1,  0, bottomLeftArc)
    shift(p, -1,  0,  0, -1, topLeftArc)
    p.closePath()
    p
  }
}
