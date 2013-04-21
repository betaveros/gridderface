package gridderface

import java.awt.Graphics2D
import scala.collection.immutable.TreeMap

// a list of GriddableGrids which keeps track of its own order
// as well as one active grid in the list for modification.
class GriddableGridList extends Griddable with ContentPutter {
  private var _currentGrid = new GriddableGrid()
  private var _currentGridIndex = 0
  private var _list: List[GriddableGrid] = List(_currentGrid)
  listenTo(_currentGrid)
  reactions += {
    case GriddableChanged(g) if g != this => publish(GriddableChanged(this))
  }
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    _list foreach {_.grid(prov, g2d)}
  }
  def put(p: Position, g: Griddable) = {
    _currentGrid.put(p, g)
  }
  def putCell(p: CellPosition, c: RectContent) =
    put(p, new CellGriddable(c, p))
  def putEdge(p: EdgePosition, c: LineContent) =
    put(p, new EdgeGriddable(c, p))
  def putIntersection(p: IntersectionPosition, c: PointContent) =
    put(p, new IntersectionGriddable(c, p))
  def clearGrid() = {
    _currentGrid.clear()
  }
  def addGrid() = {
    _currentGrid = new GriddableGrid()
    listenTo(_currentGrid)
    _list = _list :+ _currentGrid
    _currentGridIndex = _list.length - 1
  }
  def removeGrid() = {
    if (_list.length == 1) {
      // I'm not sure, but I want to insist that GriddableGridLists be nonempty
      _currentGrid = new GriddableGrid()
      listenTo(_currentGrid)
      _list = List(_currentGrid)
    } else {
      _list = _list filter (_currentGrid != _)
      _currentGrid = _list.last
      _currentGridIndex = _list.length - 1
    }
    publish(GriddableChanged(this))
  }
  def selectPreviousGrid() = {
    _currentGridIndex = (_currentGridIndex - 1 + _list.length) % _list.length
    _currentGrid = _list(_currentGridIndex)
  }
  def selectNextGrid() = {
    _currentGridIndex = (_currentGridIndex + 1) % _list.length
    _currentGrid = _list(_currentGridIndex)
  }
  def status = {
    "%d/%d".format(_currentGridIndex + 1, _list.length)
  }
}
