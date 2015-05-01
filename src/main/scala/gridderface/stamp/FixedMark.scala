package gridderface.stamp

import java.awt.Shape
import java.awt.Graphics2D
import java.awt.geom._
import java.awt.Paint
import java.awt.Stroke

abstract class FixedMark(shapes: Seq[Shape], drawVal: DrawVal) extends LineStamp with PointStamp {

  def preparedCopy(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double): Graphics2D = {
    val copy = g2d.create().asInstanceOf[Graphics2D]
    copy.translate(x, y)
    copy.scale(r, r)
    copy.setPaint(paint)
    copy
  }
  def drawPoint(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double): Unit = {
    val ng = preparedCopy(g2d, paint, x, y, r)
    for (s <- shapes) drawVal.draw(ng, s)
  }

  def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double, _tDim: Double): Unit = {
    val xd = x2 - x1
    val yd = y2 - y1
    drawPoint(g2d, paint, (x1 + x2)/2, (y1 + y2)/2, scala.math.sqrt(xd*xd + yd*yd))
  }
}

case class CrossFixedMark(size: Double, sv: StrokeVal = NormalStrokeVal)
extends FixedMark(List(new Line2D.Double(-size, -size, size, size),
    new Line2D.Double(-size, size, size, -size)),
  Draw(sv))

case class CircleFixedMark(size: Double, dv: DrawVal = Draw(NormalStrokeVal))
extends FixedMark(List(new Ellipse2D.Double(-size, -size, 2*size, 2*size)), dv)

case class SquareFixedMark(size: Double, dv: DrawVal = Draw(NormalStrokeVal))
extends FixedMark(List(new Rectangle2D.Double(-size, -size, 2*size, 2*size)), dv)
