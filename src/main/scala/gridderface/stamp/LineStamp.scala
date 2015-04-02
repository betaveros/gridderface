package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Paint

trait LineStamp {
  def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double, transverseDimension: Double): Unit
}
