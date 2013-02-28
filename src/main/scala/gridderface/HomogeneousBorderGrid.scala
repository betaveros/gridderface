package gridderface

import java.awt.Graphics2D

class HomogeneousBorderGrid(val content: LineContent, val rowStart: Int, val colStart: Int, val rowCount: Int, val colCount: Int) extends Griddable {
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    def gridEdge(r: Int, c: Int, o: EdgeOrientation.Value) = {
      new EdgeGriddable(content, new EdgePosition(r, c, o)).grid(prov, g2d)
    }
    for (r <- List(rowStart, (rowStart + rowCount))){
      for (c <- colStart until (colStart + colCount)){
        gridEdge(r, c, EdgeOrientation.Horizontal)
      }
    }
    for (c <- List(colStart, (colStart + colCount))){
      for (r <- rowStart until (rowStart + rowCount)){
        gridEdge(r, c, EdgeOrientation.Vertical)
      }
    }
  }

}
