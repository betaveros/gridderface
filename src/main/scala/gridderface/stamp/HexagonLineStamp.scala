package gridderface.stamp

import java.awt.Stroke
import java.awt.Graphics2D

import java.awt.geom.Path2D

object HexagonLineStamp {
  val hexagon = new Path2D.Float
  hexagon.moveTo(0, 0)
  for ((x,y) <- List((0.25, 0.25), (0.75, 0.25), (1.0, 0.0), (0.75, -0.25), (0.25, -0.25)))
    hexagon.lineTo(x, y)
  hexagon.closePath
}
case class HexagonLineStamp(sv: StrokeVal) extends ScalableLineStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d.setStroke(sv.stroke)
    g2d.draw(HexagonLineStamp.hexagon)
  }
}
