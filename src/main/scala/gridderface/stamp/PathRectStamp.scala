package gridderface.stamp

import java.awt.Graphics2D
import java.awt.Stroke
import java.awt.Shape
import java.awt.geom._

class PathRectStamp(val shapes: Seq[Shape], val roundShapes: Seq[Shape] = List(), val dv: DrawVal = Draw(NormalStrokeVal)) extends ScalableRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    shapes foreach (dv.draw(g2d, _))
    roundShapes foreach (dv.drawRound(g2d, _))
  }
}

object PathRectStamp {
  val majorDiagonal = new Path2D.Double
  majorDiagonal.moveTo(0.0, 0.0)
  majorDiagonal.lineTo(1.0, 1.0)
  val minorDiagonal = new Path2D.Double
  minorDiagonal.moveTo(0.0, 1.0)
  minorDiagonal.lineTo(1.0, 0.0)
  val horizontalPath = new Path2D.Double
  horizontalPath.moveTo(0.25, 0.5)
  horizontalPath.lineTo(0.75, 0.5)
  val verticalPath = new Path2D.Double
  verticalPath.moveTo(0.5, 0.25)
  verticalPath.lineTo(0.5, 0.75)
  val bigCheckPath = new Path2D.Double
  bigCheckPath.moveTo(0.25, 0.5)
  bigCheckPath.lineTo(0.45, 0.8)
  bigCheckPath.quadTo(0.6, 0.35, 0.75, 0.2)
  val smallCheckPath = new Path2D.Double
  smallCheckPath.moveTo(0.65, 0.75)
  smallCheckPath.lineTo(0.75, 0.9)
  smallCheckPath.lineTo(0.9, 0.6)
  val smallCrossPath = new Path2D.Double
  smallCrossPath.moveTo(0.25, 0.25)
  smallCrossPath.lineTo(0.75, 0.75)
  smallCrossPath.moveTo(0.25, 0.75)
  smallCrossPath.lineTo(0.75, 0.25)

  def createStar(arms: Int, outerRadius: Double, innerRadius: Double, baseAngle: Double) = {
    val angle = Math.PI / arms
    def polarPair(incs: Int, r: Double): (Double, Double) = {
      val a = baseAngle + incs * angle
      (Math.cos(a) * r, Math.sin(a) * r)
    }
    val path = new Path2D.Double
    for (i <- 0 until arms) {
      val (ox, oy) = polarPair(2*i, outerRadius)
      if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
      val (ix, iy) = polarPair(2*i + 1, innerRadius)
      path.lineTo(ix, iy)
    }
    path.closePath()
    path
  }
  val starPath = createStar(5, 0.375, 0.375 * (3 - Math.sqrt(5)) / 2, - Math.PI / 2)
  starPath.transform(AffineTransform.getTranslateInstance(0.5, 0.515625))

  def createArrowPath(dx: Int, dy: Int) = {
    val ctx: Float = dx * 0.25f // coords of tip of arrow
    val cty: Float = dy * 0.25f // from center of cell

    val path: Path2D = new Path2D.Float()
    path.moveTo(-ctx, -cty)
    path.lineTo(0, 0)
    path.moveTo(cty, ctx)
    path.lineTo(ctx, cty)
    path.lineTo(-cty, -ctx)

    path.transform(AffineTransform.getTranslateInstance(0.5, 0.5))

    path
  }
  def createAuxArrowPath(dx: Int, dy: Int) = {
    val ctx: Float = dx * 0.25f // coords of tip of arrow
    val cty: Float = dy * 0.25f // from center of cell

    val path: Path2D = new Path2D.Float()
    path.moveTo(0, 0)
    path.lineTo(ctx, cty)

    path.transform(AffineTransform.getTranslateInstance(0.5, 0.5))

    path
  }
}

import PathRectStamp._

case object  MajorDiagonalStamp extends PathRectStamp(List(), List(majorDiagonal))
case object  MinorDiagonalStamp extends PathRectStamp(List(), List(minorDiagonal))
case object       BigCrossStamp extends PathRectStamp(List(), List(majorDiagonal, minorDiagonal))
case object     SmallCrossStamp extends PathRectStamp(List(smallCrossPath))
case object       BigCheckStamp extends PathRectStamp(List(  bigCheckPath))
case object     SmallCheckStamp extends PathRectStamp(List(smallCheckPath))
case object HorizontalLineStamp extends PathRectStamp(List(horizontalPath))
case object   VerticalLineStamp extends PathRectStamp(List(  verticalPath))
case object PlusStamp extends PathRectStamp(List(horizontalPath, verticalPath))
case object StarStamp extends PathRectStamp(List(starPath), List(), Fill)

case class ArrowStamp(dx: Int, dy: Int) extends PathRectStamp(List(createArrowPath(dx, dy)), List(createAuxArrowPath(dx, dy)))
