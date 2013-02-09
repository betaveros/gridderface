package gridderface

import java.awt.Graphics2D

class HomogeneousIntersectionGrid(val content: PointContent, val rows: Int, val cols: Int) extends Griddable {
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    def gridIntersection(r: Int, c: Int) = {
      new IntersectionGriddable(content, new IntersectionPosition(r, c)).grid(prov, g2d)
    }
    for (r <- 0 to rows){
      for (c <- 0 to cols){
        gridIntersection(r, c)
      }
    }
  }
}