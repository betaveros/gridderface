package gridderface.stamp

import java.awt._
import java.awt.geom._

case class OneTextRectStamp(string: String,
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
  val Normal = FontSize.Normal
  val Small = FontSize.Small
  def fontOf(fs: FontSize.Value) = fs match {
    case Normal => normalFont
    case Small => smallFont
  }
  def stringify(fs: FontSize.Value) = fs match {
    case Normal => "n"
    case Small => "s"
  }
  def parse(s: String): Option[FontSize.Value] = s match {
    case "n" => Some(Normal)
    case "s" => Some(Small)
    case _ => None
  }
}
