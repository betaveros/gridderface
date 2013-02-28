package gridderface

import java.awt.Graphics2D

class HomogeneousIntersectionGrid(val content: PointContent, val rowStart: Int, val colStart: Int, val rowCount: Int, val colCount: Int) extends Griddable {
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    def gridIntersection(r: Int, c: Int) = {
      new IntersectionGriddable(content, new IntersectionPosition(r, c)).grid(prov, g2d)
    }
    for (r <- rowStart to (rowStart + rowCount)){
      for (c <- colStart to (colStart + colCount)){
        gridIntersection(r, c)
      }
    }
  }
}
