package gridderface

import java.awt.Graphics2D
import java.awt.Paint
import gridderface.stamp._

class CombinedRectContent(val contents: Seq[RectContent]) extends RectContent {
  def draw(g2d: Graphics2D, x: Double, y: Double, w: Double, h: Double) {
    contents foreach (_.draw(g2d, x, y, w, h))
  }
}

object CombinedRectContent {
  def apply(contents: RectContent*) = new CombinedRectContent(contents)
  def withBackground(background: Paint, content: RectContent) =
    CombinedRectContent(new RectStampContent(FullRectStamp(Fill), background),
        content)
}
