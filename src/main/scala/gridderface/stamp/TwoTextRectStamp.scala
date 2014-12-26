package gridderface.stamp

import java.awt._
import java.awt.geom._

case class TwoTextRectStamp(string1: String, string2: String) extends TextRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    prepare(g2d)
    g2d.setFont(TwoTextRectStamp.normalFont)
    drawCentered(g2d, 8.0f, 8.0f, string1)
    drawCentered(g2d, 16.0f, 16.0f, string2)
  }
}
object TwoTextRectStamp {
  val normalFont = TextRectStamp.font16
}
