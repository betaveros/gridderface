package gridderface

import java.awt.Graphics2D

class CellGriddable(content: RectContent, position: CellPosition) extends Griddable {
  override def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    CellGriddable.drawOnGrid(content, position, grid, g2d)
  }
}
object CellGriddable {
  def drawOnGrid(content: RectContent, position: CellPosition, grid: SimpleGrid, g2d: Graphics2D) {
    val row = position.row
    val col = position.col
    val x1 = grid.computeX(col)
    val x2 = grid.computeX(col + 1)
    val y1 = grid.computeY(row)
    val y2 = grid.computeY(row + 1)

    content.draw(g2d, x1, y1, x2 - x1, y2 - y1)
  }
}
