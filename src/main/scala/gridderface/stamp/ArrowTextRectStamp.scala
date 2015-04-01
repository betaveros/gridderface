package gridderface.stamp

import java.awt.{ Graphics2D, Shape }
import java.awt.geom._

case class ArrowTextRectStamp(string: String,
  arrow: ArrowTextArrow,
  sv: StrokeVal = ThinStrokeVal) extends TextRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke sv.stroke
    arrow.shapes foreach (g2d draw _)
    prepare(g2d)
    g2d.setFont(ArrowTextRectStamp.normalFont)
    drawAligned(g2d, 0f, 0f, 24.0f, 24.0f, arrow.horizontalAlignment, arrow.verticalAlignment, string)
  }
}
object ArrowTextRectStamp {
  val normalFont = TextRectStamp.font16
}
sealed abstract class ArrowTextArrow(
  val horizontalAlignment: Float,
  val verticalAlignment: Float,
  val shapes: Seq[Shape])
case object UpArrow extends ArrowTextArrow(
  0.625f, 0.5f,
  List(
    new Line2D.Float(0.25f, 0.25f, 0.25f, 0.75f),
    new Line2D.Float(0.125f, 0.375f, 0.25f, 0.25f),
    new Line2D.Float(0.375f, 0.375f, 0.25f, 0.25f)
  ))
case object DownArrow extends ArrowTextArrow(
  0.625f, 0.5f,
  List(
    new Line2D.Float(0.25f, 0.25f, 0.25f, 0.75f),
    new Line2D.Float(0.125f, 0.625f, 0.25f, 0.75f),
    new Line2D.Float(0.375f, 0.625f, 0.25f, 0.75f)
  ))
case object RightArrow extends ArrowTextArrow(
  0.5f, 0.75f,
  List(
    new Line2D.Float(0.25f, 0.25f, 0.75f, 0.25f),
    new Line2D.Float(0.625f, 0.125f, 0.75f, 0.25f),
    new Line2D.Float(0.625f, 0.375f, 0.75f, 0.25f)
  ))
case object LeftArrow extends ArrowTextArrow(
  0.5f, 0.75f,
  List(
    new Line2D.Float(0.25f, 0.25f, 0.75f, 0.25f),
    new Line2D.Float(0.375f, 0.125f, 0.25f, 0.25f),
    new Line2D.Float(0.375f, 0.375f, 0.25f, 0.25f)
  ))
