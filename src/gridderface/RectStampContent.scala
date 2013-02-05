package gridderface

import java.awt.Graphics2D
import java.awt.Paint
import gridderface.stamp.RectStamp

class RectStampContent(stamp: RectStamp, paint: Paint) extends RectContent {

  def draw(g2d: Graphics2D, x: Double, y: Double, w: Double, h: Double): Unit = {
    stamp.drawRect(g2d, paint, x, y, w, h)
  }

}