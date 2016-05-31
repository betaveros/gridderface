package gridderface

import java.awt.Graphics2D
import scala.collection.immutable.TreeMap
import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom

// a list of GriddablePositionMaps which keeps track of its own order
// and possible overriding SimpleGrids for each Map
// as well as one active map in the list for modification
//
// I lack a concise name, so here's a vague one
class GriddableModel extends Griddable with ContentPutter {
  import GriddableModel._
  private var _currentEntry = Entry()
  private var _currentEntryIndex = 0
  private var _list: List[Entry] = List(_currentEntry)
  listenTo(_currentEntry)
  reactions += {
    case GriddableChanged(g) if g != this => publish(GriddableChanged(this))
  }
  def foreach(f: Entry => Unit): Unit = _list foreach f
  // vvv I just copied this from the List documentation vvv
  def flatMap[B, That](f: Entry => GenTraversableOnce[B])(implicit bf: CanBuildFrom[List[Entry], B, That]): That = _list.flatMap(f)(bf)
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit =
    foreach(_.drawOnGrid(grid, g2d))
  def put(p: Position, g: Griddable) = {
    _currentEntry.griddableMap.put(p, g)
  }
  def get(p: Position) = {
    _currentEntry.griddableMap.get(p)
  }
  def putCell(p: CellPosition, c: RectContent) =
    put(p, new CellGriddable(c, p))
  def putEdge(p: EdgePosition, c: LineContent) =
    put(p, new EdgeGriddable(c, p))
  def putIntersection(p: IntersectionPosition, c: PointContent) =
    put(p, new IntersectionGriddable(c, p))
  def clearGrid() = {
    _currentEntry.griddableMap.clear()
  }
  def mapUpdateCurrent(f: Griddable => Griddable) {
    _currentEntry.griddableMap mapUpdate f
  }
  def clearAll() = {
    _list foreach {_.griddableMap.clear()}
  }
  def addGrid() = {
    _currentEntry = Entry()
    listenTo(_currentEntry)
    _list = _list :+ _currentEntry
    _currentEntryIndex = _list.length - 1
  }
  def removeGrid() = {
    if (_list.length == 1) {
      // I'm not sure, but I want to insist that GriddableModels be nonempty
      _currentEntry = Entry()
      listenTo(_currentEntry)
      _list = List(_currentEntry)
    } else {
      _list = _list filter (_currentEntry != _)
      _currentEntry = _list.last
      _currentEntryIndex = _list.length - 1
    }
    publish(GriddableChanged(this))
  }
  def removeAll() = {
    _currentEntry = Entry()
    listenTo(_currentEntry)
    _list = List(_currentEntry)
    _currentEntryIndex = 0
    publish(GriddableChanged(this))
  }
  def selectPreviousGrid() = {
    _currentEntryIndex = (_currentEntryIndex - 1 + _list.length) % _list.length
    _currentEntry = _list(_currentEntryIndex)
    publish(GriddableChanged(this))
  }
  def selectNextGrid() = {
    _currentEntryIndex = (_currentEntryIndex + 1) % _list.length
    _currentEntry = _list(_currentEntryIndex)
    publish(GriddableChanged(this))
  }
  def currentGridOverride = _currentEntry.gridOverride
  def currentGridOverride_=(grid: Option[SimpleGrid]) = {
    _currentEntry.gridOverride = grid
    publish(GriddableChanged(this))
  }
  def status = {
    "%d/%d".format(_currentEntryIndex + 1, _list.length)
  }
}
object GriddableModel {
  case class Entry(griddableMap: GriddablePositionMap = new GriddablePositionMap(), private var _gridOverride: Option[SimpleGrid] = None) extends Griddable {
    listenTo(griddableMap)
    reactions += {
      case GriddableChanged(g) if g != this => publish(GriddableChanged(this))
    }
    def gridOverride = _gridOverride
    def gridOverride_=(grid: Option[SimpleGrid]): Unit = {
      if (_gridOverride equals grid) return
      _gridOverride = grid
      publish(GriddableChanged(this))
    }
    def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
      griddableMap.drawOnGrid(gridOverride.getOrElse(grid), g2d)
    }
  }
}
