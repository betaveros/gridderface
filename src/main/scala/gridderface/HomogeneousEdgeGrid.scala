package gridderface

import java.awt.Graphics2D

class HomogeneousEdgeGrid(val content: LineContent, val rowStart: Int, val colStart: Int, val rowCount: Int, val colCount: Int) extends Griddable {
  
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    def gridEdge(r: Int, c: Int, o: EdgeOrientation.Value) = {
      new EdgeGriddable(content, new EdgePosition(r, c, o)).drawOnGrid(grid, g2d)
    }
    for (r <- rowStart to (rowStart + rowCount)){
      for (c <- colStart to (colStart + colCount)){
        if (r != (rowStart + rowCount)) gridEdge(r, c, EdgeOrientation.Vertical)
        if (c != (colStart + colCount)) gridEdge(r, c, EdgeOrientation.Horizontal)
      }
    }
  }

}
