package gridderface.stamp
import java.awt._

sealed abstract class DrawVal {
  def draw(g2d: Graphics2D, shape: Shape)
}
case class Draw(strokeVal: StrokeVal) extends DrawVal {
  def draw(g2d: Graphics2D, shape: Shape) {
    g2d setStroke strokeVal.stroke
    g2d draw shape
  }
}
case object Fill extends DrawVal {
  def draw(g2d: Graphics2D, shape: Shape) {
    g2d fill shape
  }
}
case class FillDraw(strokeVal: StrokeVal) extends DrawVal {
  def draw(g2d: Graphics2D, shape: Shape) {
    g2d fill shape
    g2d setStroke strokeVal.stroke
    g2d draw shape
  }
}
