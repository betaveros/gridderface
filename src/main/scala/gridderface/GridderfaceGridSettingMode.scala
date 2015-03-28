package gridderface

import scala.swing.event.MouseEvent
import java.awt.Graphics2D

class GridderfaceGridSettingMode(private var _grid: SimpleGrid, private var _rowCount: Int, private var _colCount: Int) extends GridderfaceMode with GridProvider with Griddable {
  val name = "Grid"
  def grid: SimpleGrid = _grid
  def grid_=(g: SimpleGrid): Unit = {
    if (_grid equals g) return
    _grid = g
    publish(GridChanged())
    publish(StatusChanged(this))
  }
  def moveGrid(xd: Int, yd: Int){
    grid = grid.offsetBy(xd * gridMultiplier, yd * gridMultiplier)
  }
  def adjustGridSize(xm: Int, ym: Int) {
    grid = grid.gridSizeAdjustedBy(gridMultiplier * xm, gridMultiplier * ym)
  }

  private def create: Griddable = HomogeneousEdgeGrid.defaultEdgeGrid(_rowCount, _colCount)
  private var _griddable = create
  def rowCount = _rowCount
  def colCount = _colCount
  def rowCount_=(v: Int): Unit = {
    _rowCount = v
    _griddable = create
    publish(GriddableChanged(this))
  }
  def colCount_=(v: Int): Unit = {
    _colCount = v
    _griddable = create
    publish(GriddableChanged(this))
  }
  def setRowColCount(r: Int, c: Int): Unit = {
    // premature optimization just to create the grid one less time?
    _rowCount = r
    _colCount = c
    _griddable = create
    publish(GriddableChanged(this))
  }
  def adjustRowCount(rd: Int): Unit = {
    _rowCount += rd
    _griddable = create
    publish(GriddableChanged(this))
  }
  def adjustColCount(cd: Int): Unit = {
    _colCount += cd
    _griddable = create
    publish(GriddableChanged(this))
  }

  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    _griddable.drawOnGrid(grid, g2d)
  }

  var gridExponent = 0
  def gridMultiplier = scala.math.pow(2.0, gridExponent)
  var negateFlag = false
  val moveGridReactions = KeyDataCombinations.keyDataXYFunction(moveGrid)
  val resizeGridReactions: PartialFunction[KeyData, Unit] = {
      case KeyTypedData('+') => adjustGridSize( 1,  1)
      case KeyTypedData('-') => adjustGridSize(-1, -1)
      case KeyTypedData('[') => adjustRowCount(-1)
      case KeyTypedData(']') => adjustRowCount( 1)
      case KeyTypedData('{') => adjustColCount(-1)
      case KeyTypedData('}') => adjustColCount( 1)
  }
  val singlyResizeGridReactions = KeyDataCombinations.keyDataShiftRCFunction(adjustGridSize)
  def status = {
    "%.2fx%.2f (M2^%d%s)".format(_grid.colWidth, _grid.rowHeight, gridExponent,
        if (negateFlag) "`" else "")
  }
  val setGridMultiplierReactions = KeyDataCombinations.keyDataDigitFunction(dig => {
    gridExponent = if (negateFlag) -dig else dig
    negateFlag = false
    publish(StatusChanged(this))
  })
  val negateMultiplierReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData('`') => negateFlag = true; publish(StatusChanged(this))
  }
  val keyListReactions = new SingletonListPartialFunction(
    moveGridReactions
    orElse resizeGridReactions orElse singlyResizeGridReactions
    orElse setGridMultiplierReactions
    orElse negateMultiplierReactions
    andThen {u: Unit => KeyComplete}
  )
  def handleCommand(prefix: Char, str: String) = Success("")
  val mouseReactions: PartialFunction[MouseEvent, Unit] = Map.empty
}
