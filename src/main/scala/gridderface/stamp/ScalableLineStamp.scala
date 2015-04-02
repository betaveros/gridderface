package gridderface.stamp

import java.awt._
import java.awt.geom._
import scala.math.{ sqrt, pow }

trait ScalableLineStamp extends LineStamp {
  def drawUnit(g2d: Graphics2D): Unit
  val unitLine = new Line2D.Double(0, 0, 1, 0)
  override def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double, tDim: Double): Unit = {
    preparedCopy(g2d, paint, x1, y1, x2, y2, tDim) foreach drawUnit
  }
  def isFlipped(x0: Double, y0: Double, x1: Double, y1: Double): Boolean = false
  def makeTransform(x0: Double, y0: Double, x1: Double, y1: Double, tDim: Double): AffineTransform = {
    val dx = x1 - x0
    val dy = y1 - y0
    val sign = if (isFlipped(x0, y0, x1, y1)) -1 else 1
    val mult0 = sign * tDim / sqrt(pow(dx, 2) + pow(dy, 2))
    val mult = if (mult0 == 0 || mult0.isInfinity) sign else mult0
    new AffineTransform(dx, dy, mult * -dy, mult * dx, x0, y0)
  }
  protected def preparedCopy(g2d: Graphics2D, paint: Paint,
    x1: Double, y1: Double, x2: Double, y2: Double, tDim: Double): Option[Graphics2D] = {
    if (x1 != x2 || y1 != y2) {
      val copy = g2d.create().asInstanceOf[Graphics2D]
      copy setPaint paint
      copy transform makeTransform(x1, y1, x2, y2, tDim)
      Some(copy)
    } else None
  }
}
