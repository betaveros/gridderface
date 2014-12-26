package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.Shape
import java.awt.geom.Path2D

class PathRectStamp(val shapes: Seq[Shape], val sv: StrokeVal = NormalStrokeVal) extends ScalableRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke sv.stroke
    shapes foreach (g2d draw _)
  }
}

object PathRectStamp {
  val slash1Path = new Path2D.Double
  slash1Path.moveTo(0.0, 0.0)
  slash1Path.lineTo(1.0, 1.0)
  val slash2Path = new Path2D.Double
  slash2Path.moveTo(0.0, 1.0)
  slash2Path.lineTo(1.0, 0.0)
  val checkPath = new Path2D.Double
  checkPath.moveTo(0.65, 0.75)
  checkPath.lineTo(0.75, 0.9)
  checkPath.lineTo(0.9, 0.6)

  val crossStamp = new PathRectStamp(List(slash1Path, slash2Path))
  val slash1Stamp = new PathRectStamp(List(slash1Path))
  val slash2Stamp = new PathRectStamp(List(slash2Path))
  val checkStamp = new PathRectStamp(List(checkPath))
}
