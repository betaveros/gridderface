package gridderface.stamp

import java.awt._
import java.awt.geom._

class TextRectStamp(string: String, font: Font = TextRectStamp.normalFont,
  antiAliasRenderingHint: AnyRef = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) extends ScalableRectStamp {
  val magicScale = 1.0 / 24.0
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d.scale(magicScale, magicScale)
    g2d.setFont(font)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint)
    
    val fm = g2d.getFontMetrics()

    val width = fm.stringWidth(string)
    val descent = fm.getDescent()
    val height = fm.getAscent() + descent

    val baseX = 12.0f - width / 2.0f
    val baseY = 12.0f + height / 2.0f - descent
    // stack overflow <3

    g2d.drawString(string, baseX, baseY)
  }

}
object TextRectStamp {
  val normalFont = new Font("Arial", Font.PLAIN, 20)
  val smallFont = new Font("Arial", Font.PLAIN, 12)
}