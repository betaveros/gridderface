package gridderface.stamp

import java.awt._
import java.awt.geom.Rectangle2D
// Too many small classes so I'm putting them together.

trait ScalableRectStamp extends RectStamp {
  def drawUnit(g2d: Graphics2D): Unit
  val unitRect = new Rectangle2D.Double(0, 0, 1, 1)
  override def drawRect(g2d: Graphics2D, paint: Paint, x: Double, y: Double, w: Double, h: Double): Unit = {
    preparedCopy(g2d, paint, x, y, w, h) foreach drawUnit
  }
  protected def preparedCopy(g2d: Graphics2D, paint: Paint,
    x: Double, y: Double, w: Double, h: Double): Option[Graphics2D] = {
    if (w > 0.0 && h > 0.0) {
      val copy = g2d.create().asInstanceOf[Graphics2D]
      copy.translate(x, y)
      copy.scale(w, h)
      copy.setPaint(paint)
      Some(copy)
    } else None
  }
}