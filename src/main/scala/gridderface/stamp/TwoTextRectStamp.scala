package gridderface.stamp

import java.awt._
import java.awt.geom._

class TwoTextRectStamp(string1: String, string2: String, font: Font = TwoTextRectStamp.normalFont,
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
    drawCentered(g2d, 8.0f, 8.0f, string1)
    drawCentered(g2d, 16.0f, 16.0f, string2)
  }
}
object TwoTextRectStamp {
  val normalFont = new Font("Arial", Font.PLAIN, 16)
}
