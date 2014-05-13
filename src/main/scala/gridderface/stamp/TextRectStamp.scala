package gridderface.stamp

import java.awt._
import java.awt.geom._

class TextRectStamp(string: String, font: Font = TextRectStamp.normalFont,
  horizontalAlignment: Float = 0.5f,
  verticalAlignment: Float = 0.5f,
  antiAliasRenderingHint: AnyRef = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
  ) extends ScalableRectStamp {
  val magicScale = 1.0 / 24.0
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d.scale(magicScale, magicScale)
    g2d.setFont(font)
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint)

    val fm = g2d.getFontMetrics()

    val width = fm.stringWidth(string)
    val descent = fm.getDescent()
    val height = fm.getAscent() + descent

    val baseX0 = 0f
    val baseX1 = 24.0f - width

    val baseY0 = height - descent
    val baseY1 = 24.0f - descent

    val baseX = baseX0 + (baseX1 - baseX0) * horizontalAlignment
    val baseY = baseY0 + (baseY1 - baseY0) * verticalAlignment
    // stack overflow <3

    g2d.drawString(string, baseX, baseY)
  }

}
object TextRectStamp {
  val normalFont = new Font("Arial", Font.PLAIN, 20)
  val smallFont = new Font("Arial", Font.PLAIN, 12)
}
