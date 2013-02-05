package gridderface

import scala.swing.Panel
import java.awt.Color
import java.awt.Graphics2D
import scala.collection.immutable._
import gridderface.stamp._
import scala.collection.mutable.ListBuffer

class GridPanel(val provider: GridProvider) extends Panel {
  var griddables: ListBuffer[Griddable] = ListBuffer()
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
    griddables.foreach(gb => gb.grid(provider, g))
  }
  
}