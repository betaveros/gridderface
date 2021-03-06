package gridderface

import java.awt.Graphics2D
import scala.collection.immutable._

class GriddablePositionMap extends Griddable with ContentPutter {
  var map: SortedMap[Position, Griddable] = new TreeMap[Position, Griddable]
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    map foreach {_._2.drawOnGrid(grid, g2d)}
  }
  def get(p: Position) = map get p
  def put(p: Position, g: Griddable) = {
    map += ((p, g))
    publish(GriddableChanged(this))
  }
  def putCell(p: CellPosition, c: RectContent) =
    put(p, new CellGriddable(c, p))
  def putEdge(p: EdgePosition, c: LineContent) =
    put(p, new EdgeGriddable(c, p))
  def putIntersection(p: IntersectionPosition, c: PointContent) =
    put(p, new IntersectionGriddable(c, p))
  def clear() {
    map = new TreeMap[Position, Griddable]
    publish(GriddableChanged(this))
  }
  def mapUpdate(f: Griddable => Griddable) {
    map = map mapValues f
    publish(GriddableChanged(this))
  }
}
