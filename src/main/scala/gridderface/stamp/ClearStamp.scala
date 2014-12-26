package gridderface.stamp

import java.awt._
import java.awt.geom.Rectangle2D
// Boring do-nothing instances

case object ClearStamp extends RectStamp with LineStamp with PointStamp {
  // do nothing
  def drawRect(g2d: Graphics2D, paint: Paint, x: Double, y: Double, w: Double, h: Double) = Unit
  def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double) = Unit
  def drawPoint(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double) = Unit
}
