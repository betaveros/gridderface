package gridderface.stamp

import java.awt.Shape
import java.awt.Graphics2D
import java.awt.geom._
import java.awt.Paint
import java.awt.Stroke

abstract class FixedMark(filledShapes: Seq[Shape], drawnShapes: Seq[Shape], stroke: Option[Stroke]) extends LineStamp with PointStamp {

  def preparedCopy(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double): Graphics2D = {
    val copy = g2d.create().asInstanceOf[Graphics2D]
    copy.translate(x, y)
    copy.scale(r, r)
    copy.setPaint(paint)
    copy
  }
  def drawPoint(g2d: Graphics2D, paint: Paint, x: Double, y: Double, r: Double): Unit = {
    val ng = preparedCopy(g2d, paint, x, y, r)
    for (s <- filledShapes) ng.fill(s)
      stroke foreach (st => { ng.setStroke(st); for (s <- drawnShapes) ng.draw(s) })
  }

  def drawLine(g2d: Graphics2D, paint: Paint, x1: Double, y1: Double, x2: Double, y2: Double): Unit = {
    val xd = x2 - x1
    val yd = y2 - y1
    drawPoint(g2d, paint, (x1 + x2)/2, (y1 + y2)/2, scala.math.sqrt(xd*xd + yd*yd))
  }
}

case class CrossFixedMark(size: Double, sv: StrokeVal = NormalStrokeVal)
extends FixedMark(Nil,
  List(new Line2D.Double(-size, -size, size, size),
    new Line2D.Double(-size, size, size, -size)),
  Some(sv.stroke))

case class CircleFixedMark(size: Double, sv: StrokeVal = NormalStrokeVal)
extends FixedMark(Nil,
  List(new Ellipse2D.Double(-size, -size, 2*size, 2*size)),
  Some(sv.stroke))

case class DiskFixedMark(size: Double)
extends FixedMark(List(new Ellipse2D.Double(-size, -size, 2*size, 2*size)),
  Nil, None)

case class FilledSquareFixedMark(size: Double)
extends FixedMark(List(new Rectangle2D.Double(-size, -size, 2*size, 2*size)),
  Nil, None)
