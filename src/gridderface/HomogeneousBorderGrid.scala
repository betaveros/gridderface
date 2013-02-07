package gridderface

import java.awt.Graphics2D

class HomogeneousBorderGrid(val content: LineContent, val rows: Int, val cols: Int) extends Griddable {
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    def gridEdge(r: Int, c: Int, o: EdgeOrientation.Value) = {
      new EdgeGriddable(content, new EdgePosition(r, c, o)).grid(prov, g2d)
    }
    for (r <- List(0, rows)){
      for (c <- 0 until cols){
        gridEdge(r, c, EdgeOrientation.Horizontal)
      }
    }
    for (c <- List(0, cols)){
      for (r <- 0 until rows){
        gridEdge(r, c, EdgeOrientation.Vertical)
      }
    }
  }

}