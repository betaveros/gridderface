package gridderface.stamp

import java.awt._
import java.awt.geom._

abstract class TextRectStamp extends ScalableRectStamp {
  val magicScale = 1.0 / 24.0
  def prepare(g2d: Graphics2D): Unit = {
    g2d.scale(magicScale, magicScale)
  }
  def drawAligned(g2d: Graphics2D, lowX: Float, lowY: Float, highX: Float, highY: Float, horizontalAlignment: Float, verticalAlignment: Float, string: String): Unit = {
    val fm = g2d.getFontMetrics()

    val width = fm.stringWidth(string)
    val descent = fm.getDescent()
    val height = fm.getAscent() + descent

    val baseX0 = lowX
    val baseX1 = highX - width

    val baseY0 = lowY + height - descent
    val baseY1 = highY - descent

    val baseX = baseX0 + (baseX1 - baseX0) * horizontalAlignment
    val baseY = baseY0 + (baseY1 - baseY0) * verticalAlignment
    // stack overflow <3

    g2d.drawString(string, baseX, baseY)
  }
  def drawCentered(g2d: Graphics2D, centerX: Float, centerY: Float, string: String): Unit = {
    drawAligned(g2d, centerX, centerY, centerX, centerY, 0.5f, 0.5f, string)
  }
  /*
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
  */

}
object TextRectStamp {
  val font20 = new Font("Arial", Font.PLAIN, 20)
  val font16 = new Font("Arial", Font.PLAIN, 16)
  val font14 = new Font("Arial", Font.PLAIN, 14)
  val font12 = new Font("Arial", Font.PLAIN, 12)
  val magicScale = 1.0 / 24.0
}
