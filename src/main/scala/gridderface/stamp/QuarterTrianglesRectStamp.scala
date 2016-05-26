package gridderface.stamp

import java.awt.{ Graphics2D, Point, Shape }
import java.awt.geom._

case class QuarterTrianglesRectStamp(size: Double, dv: DrawVal, topTriangle: Boolean, rightTriangle: Boolean, bottomTriangle: Boolean, leftTriangle: Boolean, xOffset: Double = 0, yOffset: Double = 0) extends ScalableRectStamp {
  val path = {
    val sx = 0.5 - size / 2.0
    if (topTriangle && rightTriangle && bottomTriangle && leftTriangle) {
      new Rectangle2D.Double(xOffset + sx, yOffset + sx, size, size)
    } else {
      val p = QuarterTrianglesRectStamp.buildQuarterTrianglesPathAtOrigin(topTriangle, rightTriangle, bottomTriangle, leftTriangle)
      p.transform(AffineTransform.getScaleInstance(size / 2.0, size / 2.0))
      p.transform(AffineTransform.getTranslateInstance(0.5, 0.5))
      p
    }
  }

  override def drawUnit(g2d: Graphics2D) = {
    dv.draw(g2d, path)
  }
}
object QuarterTrianglesRectStamp {
  val points: List[(Double, Double)] = List((1,-1), (1,1), (-1,1), (-1,-1))
  def buildQuarterTrianglesPathAtOrigin(bools: Boolean*): Path2D.Double = {
    val p = new Path2D.Double
    p.moveTo(-1, -1)
    var active = false
    for (((x, y), b) <- points zip bools) {
      if (b) {
        p.lineTo(x, y)
        active = true
      } else {
        if (active) {
          p.lineTo(0, 0)
          p.closePath()
          active = false
        }
        p.moveTo(x, y)
      }
    }
    if (active) {
      p.lineTo(0, 0)
      p.closePath()
      active = false
    }
    p
  }
}
