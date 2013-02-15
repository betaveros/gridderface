package gridderface.stamp

import java.awt._

class StrokeLineStamp(stroke: Stroke) extends ScalableLineStamp {
  def this(width: Float) = this(new BasicStroke(width))
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d.setStroke(stroke)
    g2d.draw(unitLine)
  }

}

object Strokes {
  val normalWidth = 0.125f;
  val normalStroke = new BasicStroke(normalWidth)
  val normalStamp = new StrokeLineStamp(normalStroke)
  
  val thickWidth = 0.25f;
  val thickStroke = new BasicStroke(thickWidth)
  val thickStamp = new StrokeLineStamp(thickStroke)
  
  val thinWidth = 0.0625f;
  val thinStroke = new BasicStroke(thinWidth)
  val thinStamp = new StrokeLineStamp(thinStroke)
  
  val normalDashedStroke = createDashedStroke(normalWidth, 1.0f / 6.0f)
  val thinDashedStroke = createDashedStroke(thinWidth, 0.125f)

  val normalDashedStamp = new StrokeLineStamp(normalDashedStroke)
  val thinDashedStamp = new StrokeLineStamp(thinDashedStroke)

  def createDashedStroke(width: Float, dash: Float, gap: Float) = 
    new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
        Array(dash, gap), 0.0f)
  def createDashedStroke(width: Float, dash: Float): BasicStroke =
    createDashedStroke(width, dash, dash)
}