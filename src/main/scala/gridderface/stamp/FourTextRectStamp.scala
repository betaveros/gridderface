package gridderface.stamp

import java.awt._
import java.awt.geom._

class FourTextRectStamp(string1: String, string2: String, string3: String, string4: String, font: Font = FourTextRectStamp.normalFont,
  antiAliasRenderingHint: AnyRef = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) extends ScalableRectStamp {
  val magicScale = 1.0 / 24.0
  def drawCentered(g2d: Graphics2D, centerX: Float, centerY: Float, string: String): Unit = {
    val fm = g2d.getFontMetrics()

    val width = fm.stringWidth(string)
    val descent = fm.getDescent()
    val height = fm.getAscent() + descent

    val baseX = centerX - width / 2.0f
    val baseY = centerY + height / 2.0f - descent
    // stack overflow <3

    g2d.drawString(string, baseX, baseY)
  }
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d.scale(magicScale, magicScale)
    g2d.setFont(font)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint)
    drawCentered(g2d, 6.0f, 6.0f, string1)
    drawCentered(g2d, 18.0f, 6.0f, string2)
    drawCentered(g2d, 6.0f, 18.0f, string3)
    drawCentered(g2d, 18.0f, 18.0f, string4)
  }

}
object FourTextRectStamp {
  val normalFont = new Font("Arial", Font.PLAIN, 14)
}
