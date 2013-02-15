package gridderface.stamp

import java.awt._
import java.awt.geom._

trait ScalableLineStamp extends LineStamp {
  def drawUnit(g2d: Graphics2D): Unit
  val unitLine = new Line2D.Double(0, 0, 1, 0)
  override def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    preparedCopy(g2d, paint, x1, y1, x2, y2) foreach drawUnit
  }
  def spiralSimilarity(x0: Double, y0: Double, x1: Double, y1: Double): AffineTransform = {
    val dx = x1 - x0
    val dy = y1 - y0
    new AffineTransform(dx, dy, -dy, dx, x0, y0)
  }
  protected def preparedCopy(g2d: Graphics2D, paint: Paint,
    x1: Double, y1: Double, x2: Double, y2: Double): Option[Graphics2D] = {
    if (x1 != x2 || y1 != y2) {
      val copy = g2d.create().asInstanceOf[Graphics2D]
      copy.setPaint(paint)
      copy.transform(spiralSimilarity(x1, y1, x2, y2))
      Some(copy)
    } else None
  }
}