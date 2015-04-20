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
object DrawVal {
  private val FillChar = '+'
  def parse(s: String): Option[DrawVal] = s.headOption match {
    case None => None
    case Some(FillChar) =>
      if (s.length == 1) Some(Fill) else StrokeVal.parse(s).map(FillDraw(_))
    case _ => StrokeVal.parse(s).map(Draw(_))
  }
  def stringify(s: DrawVal): String = s match {
    case Fill => FillChar.toString
    case Draw(s) => StrokeVal.stringify(s)
    case FillDraw(s) => StrokeVal.stringify(s)
  }
}
