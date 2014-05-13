package gridderface.stamp

import java.awt.Paint
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.awt.AlphaComposite
import java.awt.TexturePaint
import java.awt.image.BufferedImage
import java.awt.Color

class TextureFillRectStamp(val texture: Paint, val textureWidth: Int, val textureHeight: Int) extends RectStamp {
  def drawRect(g2d: Graphics2D, paint: Paint, x: Double, y: Double, w: Double, h: Double) {
    val prepImg = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
    val prepRect = new Rectangle2D.Double(0, 0, textureWidth, textureHeight)
    val prepG = prepImg.createGraphics()
    prepG setPaint paint
    prepG fill prepRect
    prepG setComposite AlphaComposite.DstIn
    prepG setPaint texture
    prepG fill prepRect

    val copy = g2d.create().asInstanceOf[Graphics2D]
    copy.translate(x, y)
    val rect = new Rectangle2D.Double(0, 0, w, h)

    copy setPaint (new TexturePaint(prepImg, prepRect))
    copy fill rect
  }
}

object TextureFillRectStamp {
  private def makeSimple3x3Stamp(x1: Int, y1: Int, x2: Int, y2: Int) = {
    val bi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB)
    val g2d = bi.createGraphics();
    g2d.setColor(Color.BLACK); // doesn't really matter
    g2d.drawLine(x1, y1, x2, y2);
    new TextureFillRectStamp(new TexturePaint(bi, new Rectangle2D.Double(0,0,3,3)), 3, 3)
  }
  val diagonalStamp = makeSimple3x3Stamp(0, 0, 2, 2)
  val dashedStamp = makeSimple3x3Stamp(0, 0, 1, 0)
  val dottedStamp = makeSimple3x3Stamp(0, 0, 0, 0)
}
