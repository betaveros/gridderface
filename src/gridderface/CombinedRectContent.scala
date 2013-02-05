package gridderface

import java.awt.Graphics2D

class CombinedRectContent(val contents: Seq[RectContent]) extends RectContent {

  def draw(g2d: Graphics2D, x: Double, y: Double, w: Double, h: Double) {
    contents foreach (_.draw(g2d, x, y, w, h))
  }

}