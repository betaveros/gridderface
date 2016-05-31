package gridderface

import scala.swing.Panel
import java.awt._
import scala.collection.immutable._
import gridderface.stamp._
import scala.collection.mutable.ListBuffer
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

class GridPanel extends Panel {
  var buffers: ListBuffer[OpacityBuffer] = ListBuffer()
  private var transform = new AffineTransform()
//  sample stuff:
//  ListBuffer(
//    new CellGriddable(new RectStampContent(FillRectStamp, Color.RED),
//        new CellPosition(2,2)),
//    new CellGriddable(new RectStampContent(new TextRectStamp("3"), Color.RED),
//        new CellPosition(4,4)),
//    new EdgeGriddable(new LineStampContent(Strokes.normalStamp, Color.BLUE),
//        new EdgePosition(3,2,EdgeOrientation.Horizontal)),
//    new EdgeGriddable(new LineStampContent(Strokes.normalDashedStamp, Color.BLUE),
//        new EdgePosition(2,3,EdgeOrientation.Vertical)),
//    new EdgeGriddable(new LineStampContent(FixedMark.createCrossStamp(0.125), Color.RED),
//        new EdgePosition(2,4,EdgeOrientation.Vertical))
//  )
  override def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    buffers.foreach(b => b.draw(g, transform, size))
  }
  def translate(x: Double, y: Double) = {
    transform.translate(x, y)
    repaint()
  }
  def viewToGrid(pt: Point2D): Point2D = transform.inverseTransform(pt, null)
  def getScale() = transform.getScaleX()
  def scale(s: Double) = {transform.scale(s, s); repaint()}

}
