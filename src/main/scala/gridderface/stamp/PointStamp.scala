package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Paint

trait PointStamp {
  def drawPoint(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double): Unit
}