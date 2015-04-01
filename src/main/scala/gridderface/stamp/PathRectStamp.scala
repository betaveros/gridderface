package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.Shape
import java.awt.geom._

class PathRectStamp(val shapes: Seq[Shape], val sv: StrokeVal = NormalStrokeVal) extends ScalableRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke sv.stroke
    shapes foreach (g2d draw _)
  }
}

object PathRectStamp {
  val majorDiagonal = new Path2D.Double
  majorDiagonal.moveTo(0.0, 0.0)
  majorDiagonal.lineTo(1.0, 1.0)
  val minorDiagonal = new Path2D.Double
  minorDiagonal.moveTo(0.0, 1.0)
  minorDiagonal.lineTo(1.0, 0.0)
  val checkPath = new Path2D.Double
  checkPath.moveTo(0.65, 0.75)
  checkPath.lineTo(0.75, 0.9)
  checkPath.lineTo(0.9, 0.6)

  def createArrowPath(dx: Int, dy: Int) = {
    val path: Path2D = new Path2D.Float();
    val ctx: Float = dx * 0.25f // coords of tip of arrow
    val cty: Float = dy * 0.25f // from center of cell

    path.moveTo(-ctx, -cty)
    path.lineTo(ctx, cty)
    path.moveTo(cty, ctx)
    path.lineTo(ctx, cty)
    path.lineTo(-cty, -ctx)

    path.transform(AffineTransform.getTranslateInstance(0.5, 0.5))
    path
  }
}

case object CrossStamp extends PathRectStamp(List(PathRectStamp.majorDiagonal, PathRectStamp.minorDiagonal))
case object MajorDiagonalStamp extends PathRectStamp(List(PathRectStamp.majorDiagonal))
case object MinorDiagonalStamp extends PathRectStamp(List(PathRectStamp.minorDiagonal))
case object CheckStamp extends PathRectStamp(List(PathRectStamp.checkPath))

case class ArrowStamp(dx: Int, dy: Int) extends PathRectStamp(List(PathRectStamp.createArrowPath(dx, dy)))
