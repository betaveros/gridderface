package gridderface.stamp

import java.awt._
import java.awt.geom._

class FourTextRectStamp(string1: String, string2: String, string3: String, string4: String) extends TextRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    prepare(g2d)
    g2d.setFont(FourTextRectStamp.normalFont)
    drawCentered(g2d, 6.0f, 6.0f, string1)
    drawCentered(g2d, 18.0f, 6.0f, string2)
    drawCentered(g2d, 6.0f, 18.0f, string3)
    drawCentered(g2d, 18.0f, 18.0f, string4)
  }

}
object FourTextRectStamp {
  val normalFont = TextRectStamp.font14
}
