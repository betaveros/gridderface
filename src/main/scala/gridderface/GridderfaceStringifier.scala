package gridderface

import gridderface.stamp._
import java.awt.Color

object GridderfaceStringifier {
  def stringifyColor(c: Color): String = Seq(c.getRed(), c.getGreen(), c.getBlue()).mkString(",")
  def dumpGriddablePositionMap(m: GriddablePositionMap): Unit = {
    m.map foreach (_ match {
      case (CellPosition(r, c), CellGriddable(RectStampContent(s, color: Color), _)) => {
        println("c %d %d %s %s".format(r, c, stringifyColor(color), StampStringifier.stringifyRectStamp(s)))
      }
      case (EdgePosition(r, c, o), EdgeGriddable(LineStampContent(s, color: Color), _)) => {
        val ochar = o match {
          case EdgeOrientation.Horizontal => 'h'
          case EdgeOrientation.Vertical => 'v'
        }
        println("%c %d %d %s %s".format(ochar, r, c, stringifyColor(color), StampStringifier.stringifyLineStamp(s)))
      }
      case (IntersectionPosition(r, c), IntersectionGriddable(PointStampContent(s, color: Color), _)) => {
        println("i %d %d %s %s".format(r, c, stringifyColor(color), StampStringifier.stringifyPointStamp(s)))
      }
    })
  }
}
