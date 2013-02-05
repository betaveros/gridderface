package gridderface.stamp

import java.awt._
import java.awt.geom.Rectangle2D

trait RectStamp {
  def drawRect(g2d: Graphics2D, paint: Paint, x: Double, y: Double, w: Double, h: Double): Unit
}