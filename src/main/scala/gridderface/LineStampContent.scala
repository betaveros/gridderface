package gridderface

import java.awt.Graphics2D
import java.awt.Paint
import gridderface.stamp.LineStamp

class LineStampContent(stamp: LineStamp, paint: Paint) extends LineContent {
  def draw(g2d: Graphics2D, x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    stamp.drawLine(g2d, paint, x1, y1, x2, y2)
  }
  
}