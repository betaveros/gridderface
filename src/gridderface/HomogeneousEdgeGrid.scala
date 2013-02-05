package gridderface

import java.awt.Graphics2D

class HomogeneousEdgeGrid(val content: LineContent, val rows: Int, val cols: Int) extends Griddable {
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    def gridEdge(r: Int, c: Int, o: EdgeOrientation.Value) = {
      new EdgeGriddable(content, new EdgePosition(r, c, o)).grid(prov, g2d)
    }
    for (r <- 0 to rows){
      for (c <- 0 to cols){
        if (r != rows) gridEdge(r, c, EdgeOrientation.Vertical)
        if (c != cols) gridEdge(r, c, EdgeOrientation.Horizontal)
      }
    }
  }

}