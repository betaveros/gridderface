package gridderface.stamp

import java.awt._
import java.awt.geom._

class OneTextRectStamp(string: String,
  fontSize: OneTextRectStamp.FontSize.Value = OneTextRectStamp.FontSize.Normal,
  horizontalAlignment: Float = 0.5f,
  verticalAlignment: Float = 0.5f) extends TextRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    prepare(g2d)
    g2d.setFont(OneTextRectStamp.fontOf(fontSize))
    drawAligned(g2d, 0f, 0f, 24.0f, 24.0f, horizontalAlignment, verticalAlignment, string)
  }
}
object OneTextRectStamp {
  val normalFont = TextRectStamp.font20
  val smallFont = TextRectStamp.font12
  object FontSize extends Enumeration {
    type FontSize = Value
    val Normal, Small = Value
  }
  def fontOf(fs: FontSize.Value) = fs match {
    case FontSize.Normal => normalFont
    case FontSize.Small => smallFont
  }
}
