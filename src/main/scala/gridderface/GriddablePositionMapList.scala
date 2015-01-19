package gridderface

import java.awt.Graphics2D
import scala.collection.immutable.TreeMap
import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom

// a list of GriddablePositionMaps which keeps track of its own order
// as well as one active map in the list for modification.
class GriddablePositionMapList extends Griddable with ContentPutter {
  private var _currentMap = new GriddablePositionMap()
  private var _currentMapIndex = 0
  private var _list: List[GriddablePositionMap] = List(_currentMap)
  listenTo(_currentMap)
  reactions += {
    case GriddableChanged(g) if g != this => publish(GriddableChanged(this))
  }
  def foreach(f: GriddablePositionMap => Unit): Unit = _list foreach f
  // vvv I just copied this from the List documentation vvv
  def flatMap[B, That](f: (GriddablePositionMap) => GenTraversableOnce[B])(implicit bf: CanBuildFrom[List[GriddablePositionMap], B, That]): That = _list.flatMap(f)(bf)
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit =
    foreach(_.drawOnGrid(grid, g2d))
  def put(p: Position, g: Griddable) = {
    _currentMap.put(p, g)
  }
  def putCell(p: CellPosition, c: RectContent) =
    put(p, new CellGriddable(c, p))
  def putEdge(p: EdgePosition, c: LineContent) =
    put(p, new EdgeGriddable(c, p))
  def putIntersection(p: IntersectionPosition, c: PointContent) =
    put(p, new IntersectionGriddable(c, p))
  def clearGrid() = {
    _currentMap.clear()
  }
  def clearAll() = {
    _list foreach {_.clear()}
  }
  def addGrid() = {
    _currentMap = new GriddablePositionMap()
    listenTo(_currentMap)
    _list = _list :+ _currentMap
    _currentMapIndex = _list.length - 1
  }
  def removeGrid() = {
    if (_list.length == 1) {
      // I'm not sure, but I want to insist that GriddablePositionMapLists be nonempty
      _currentMap = new GriddablePositionMap()
      listenTo(_currentMap)
      _list = List(_currentMap)
    } else {
      _list = _list filter (_currentMap != _)
      _currentMap = _list.last
      _currentMapIndex = _list.length - 1
    }
    publish(GriddableChanged(this))
  }
  def removeAll() = {
    _currentMap = new GriddablePositionMap()
    listenTo(_currentMap)
    _list = List(_currentMap)
    _currentMapIndex = 0
    publish(GriddableChanged(this))
  }
  def selectPreviousGrid() = {
    _currentMapIndex = (_currentMapIndex - 1 + _list.length) % _list.length
    _currentMap = _list(_currentMapIndex)
  }
  def selectNextGrid() = {
    _currentMapIndex = (_currentMapIndex + 1) % _list.length
    _currentMap = _list(_currentMapIndex)
  }
  def status = {
    "%d/%d".format(_currentMapIndex + 1, _list.length)
  }
}
