package gridderface

import java.awt.Graphics2D

class IntersectionGriddable(content: PointContent, position: IntersectionPosition) extends Griddable {
  override def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    IntersectionGriddable.grid(content, position, prov, g2d)
  }
}
object IntersectionGriddable {
  def grid(content: PointContent, position: IntersectionPosition, prov: GridProvider, g2d: Graphics2D) {
    val row = position.row
    val col = position.col
    val x1 = prov.computeX(col)
    val x2 = prov.computeX(col + 1)
    val y1 = prov.computeY(row)
    val y2 = prov.computeY(row + 1)

    content.draw(g2d, x1, y1, math.min(x2 - x1, y2 - y1))
  }
}