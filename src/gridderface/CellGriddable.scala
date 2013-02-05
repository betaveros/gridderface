package gridderface

import java.awt.Graphics2D

class CellGriddable(content: RectContent, position: CellPosition) extends Griddable {
  override def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    CellGriddable.grid(content, position, prov, g2d)
  }
}
object CellGriddable {
  def grid(content: RectContent, position: CellPosition, prov: GridProvider, g2d: Graphics2D) {
    val row = position.row
    val col = position.col
    val x1 = prov.computeX(col)
    val x2 = prov.computeX(col + 1)
    val y1 = prov.computeY(row)
    val y2 = prov.computeY(row + 1)

    content.draw(g2d, x1, y1, x2 - x1, y2 - y1)
  }
}