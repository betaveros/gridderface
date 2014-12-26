package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.geom.Path2D

case class InequalityLineStamp(sv: StrokeVal, ineq: InequalityLineStamp.Inequality.Value) extends ScalableLineStamp {
  val path = new Path2D.Double()
  val sign = ineq match {
    case InequalityLineStamp.Less => 1
    case InequalityLineStamp.Greater => -1
  }
  path.moveTo(0.25, sign * 0.125)
  path.lineTo(0.50, sign * -0.125)
  path.lineTo(0.75, sign * 0.125)
  override def isFlipped(x0: Double, y0: Double, x1: Double, y1: Double): Boolean = { x1 - x0 < y1 - y0 }
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke sv.stroke
    g2d draw path
  }
}
object InequalityLineStamp {
  object Inequality extends Enumeration {
    type Inequality = Value
    val Less, Greater = Value
  }
  val Less = Inequality.Less
  val Greater = Inequality.Greater
  def getValue(c: Char) = c match {
    case '<' => Some(Less)
    case '>' => Some(Greater)
    case _ => None
  }
  def stringify(v: Inequality.Value) = v match {
    case Less => "<"
    case Greater => ">"
  }

}
