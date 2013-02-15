package gridderface

import java.awt.Graphics2D
import java.awt.Paint
import gridderface.stamp.PointStamp

class PointStampContent(stamp: PointStamp, paint: Paint) extends PointContent {
  def draw(g2d: Graphics2D, x: Double, y: Double, r: Double): Unit = {
    stamp.drawPoint(g2d, paint, x, y, r)
  }
  
}