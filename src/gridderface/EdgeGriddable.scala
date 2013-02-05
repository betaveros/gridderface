package gridderface

import java.awt.Graphics2D
import gridderface.EdgeOrientation._

class EdgeGriddable(content: LineContent, position: EdgePosition) extends Griddable {
  override def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    EdgeGriddable.grid(content, position, prov, g2d)
  }
}
object EdgeGriddable {
  def grid(content: LineContent, position: EdgePosition, prov: GridProvider, g2d: Graphics2D){
    val row = position.row
    val col = position.col
    val (row2, col2) = position.orientation match {
      case EdgeOrientation.Horizontal => (row, col+1)
      case EdgeOrientation.Vertical => (row+1, col)
    }
    val x1 = prov.computeX(col)
    val x2 = prov.computeX(col2)
    val y1 = prov.computeY(row)
    val y2 = prov.computeY(row2)

    content.draw(g2d, x1, y1, x2, y2)
  }
}