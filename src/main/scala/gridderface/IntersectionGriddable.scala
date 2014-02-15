package gridderface

import java.awt.Graphics2D

class IntersectionGriddable(content: PointContent, position: IntersectionPosition) extends Griddable {
  override def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    IntersectionGriddable.drawOnGrid(content, position, grid, g2d)
  }
}
object IntersectionGriddable {
  def drawOnGrid(content: PointContent, position: IntersectionPosition, grid: SimpleGrid, g2d: Graphics2D) {
    val row = position.row
    val col = position.col
    val x1 = grid.computeX(col)
    val x2 = grid.computeX(col + 1)
    val y1 = grid.computeY(row)
    val y2 = grid.computeY(row + 1)

    content.draw(g2d, x1, y1, math.min(x2 - x1, y2 - y1))
  }
}
