package gridderface

import java.awt.Graphics2D

case class CellGriddable(content: RectContent, position: CellPosition) extends Griddable {
  override def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    CellGriddable.drawOnGrid(content, position, grid, g2d)
  }
}
object CellGriddable {
  def drawOnGrid(content: RectContent, position: CellPosition, grid: SimpleGrid, g2d: Graphics2D) {
    val row = position.row
    val col = position.col
    val (x1, x2) = grid.computeXBounds(col)
    val (y1, y2) = grid.computeYBounds(row)

    content.draw(g2d, x1, y1, x2 - x1, y2 - y1)
  }
}
