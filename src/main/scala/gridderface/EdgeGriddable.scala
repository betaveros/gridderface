package gridderface

import java.awt.Graphics2D
import gridderface.EdgeOrientation._

class EdgeGriddable(content: LineContent, position: EdgePosition) extends Griddable {
  override def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    EdgeGriddable.drawOnGrid(content, position, grid, g2d)
  }
}
object EdgeGriddable {
  def drawOnGrid(content: LineContent, position: EdgePosition, grid: SimpleGrid, g2d: Graphics2D){
    val row = position.row
    val col = position.col
    val (row2, col2) = position.orientation match {
      case EdgeOrientation.Horizontal => (row, col+1)
      case EdgeOrientation.Vertical => (row+1, col)
    }
    val x1 = grid.computeX(col)
    val x2 = grid.computeX(col2)
    val y1 = grid.computeY(row)
    val y2 = grid.computeY(row2)

    content.draw(g2d, x1, y1, x2, y2)
  }
}
