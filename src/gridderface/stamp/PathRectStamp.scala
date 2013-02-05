package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.Shape
import java.awt.geom.Path2D


class PathRectStamp(val shapes: Seq[Shape], val stroke: Stroke) extends ScalableRectStamp {

  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke stroke
    shapes foreach (g2d draw _)
  }

}

object PathRectStamp {
  val crossPath = new Path2D.Double
  crossPath.moveTo(0.0, 0.0)
  crossPath.lineTo(1.0, 1.0)
  crossPath.moveTo(0.0, 1.0)
  crossPath.lineTo(1.0, 0.0)
  
  val crossStamp = new PathRectStamp(List(crossPath), Strokes.normalStroke)
  
}