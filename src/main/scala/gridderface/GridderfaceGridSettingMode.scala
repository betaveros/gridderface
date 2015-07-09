package gridderface

import scala.swing.event._
import java.awt.{ Graphics2D, Point }
import java.awt.geom.Point2D

class GridderfaceGridSettingMode(private var _grid: SimpleGrid,
  private var _rowCount: Int, private var _colCount: Int,
  viewed2grid: Point => Point2D) extends GridderfaceMode with GridProvider with Griddable {

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
  def snap() {
    grid = grid.offsetRounded
  }
  def snapAll() {
    grid = grid.allRounded
  }

  private def create: Griddable = HomogeneousEdgeGrid.defaultEdgeGrid(_rowCount, _colCount)
  private var _griddable = create

  // to be typesafe this ought to be an enum or something, sorry
  private var pending: Char = ' '
  private def resetPending(): Unit = {
    pending = ' '
    publish(StatusChanged(this))
  }

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
  def adjustGridExponent(d: Int): Unit = {
    gridExponent += d
    publish(StatusChanged(this))
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
      case KeyTypedData('f') => adjustGridExponent(-1)
      case KeyTypedData('c') => adjustGridExponent( 1)
      case KeyTypedData('s') => snap()
      case KeyTypedData('S') => snapAll()
      case KeyTypedData('r') => pending = 'r'; publish(StatusChanged(this))
      case KeyTypedData('m') => pending = 'm'; publish(StatusChanged(this))
  }
  val pendingResizeGridReactions: PartialFunction[KeyData, Unit] = {
      case KeyTypedData('d') => grid = SimpleGrid.defaultGrid; resetPending()
      case KeyTypedData('g') => grid = SimpleGrid.generationGrid; resetPending()
      case KeyTypedData('\u001b' /*esc*/) => resetPending()
  }
  val pendingMoveGridReactions: PartialFunction[KeyData, Unit] = {
      case KeyTypedData('r') => grid = grid.withOffset(0, 0); resetPending()
      case KeyTypedData('\u001b' /*esc*/) => resetPending()
  }
  val singlyResizeGridReactions = KeyDataCombinations.keyDataShiftRCFunction(adjustGridSize)
  def status = pending match {
    case ' ' =>
      "%.2fx%.2f (M2^%d%s)".format(_grid.colWidth, _grid.rowHeight, gridExponent,
          if (negateFlag) "`" else "")
    case 'r' => "Pending resize... (d/g/esc/drag with the mouse)"
    case 'm' => "Pending move... (r/esc/drag with the mouse)"
    case _ => throw new AssertionError("GridSettingMode has unexpected pending status")
  }
  val setGridMultiplierReactions = KeyDataCombinations.keyDataDigitFunction(dig => {
    gridExponent = if (negateFlag) -dig else dig
    negateFlag = false
    publish(StatusChanged(this))
  })
  val negateMultiplierReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData('`') => negateFlag = true; publish(StatusChanged(this))
  }
  val pendingResizeKeyListReactions = new SingletonListPartialFunction(
    pendingResizeGridReactions andThen {u: Unit => KeyComplete})
  val pendingMoveKeyListReactions = new SingletonListPartialFunction(
    pendingMoveGridReactions andThen {u: Unit => KeyComplete})
  val normalKeyListReactions = new SingletonListPartialFunction(
    moveGridReactions
    orElse resizeGridReactions orElse singlyResizeGridReactions
    orElse setGridMultiplierReactions
    orElse negateMultiplierReactions
    andThen {u: Unit => KeyComplete}
  )
  def keyListReactions = pending match {
    case 'r' => pendingResizeKeyListReactions
    case 'm' => pendingMoveKeyListReactions
    case _ => normalKeyListReactions
  }
  def handleCommand(prefix: Char, str: String) = Success("")
  var startPt: Point2D = new Point2D.Double(0, 0)
  var origXOffset: Double = 0.0
  var origYOffset: Double = 0.0
  private def drag(pt: Point) = {
    val endPt = viewed2grid(pt)
    val lx = startPt.getX min endPt.getX
    val ly = startPt.getY min endPt.getY
    val hx = startPt.getX max endPt.getX
    val hy = startPt.getY max endPt.getY
    grid = new SimpleGrid(
      (hy - ly).toDouble / _rowCount,
      (hx - lx).toDouble / _colCount,
      lx, ly)
    publish(GridChanged())
  }
  private def move(pt: Point) = {
    val endPt = viewed2grid(pt)
    val lx = startPt.getX min endPt.getX
    val ly = startPt.getY min endPt.getY
    val hx = startPt.getX max endPt.getX
    val hy = startPt.getY max endPt.getY
    grid = new SimpleGrid(
      grid.rowHeight,
      grid.colWidth,
      origXOffset + endPt.getX - startPt.getX,
      origYOffset + endPt.getY - startPt.getY)
    publish(GridChanged())
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = _ match {
    case MousePressed(_, pt, _, _, _) => pending match {
      case 'r' => startPt = viewed2grid(pt)
      case 'm' => {
        startPt = viewed2grid(pt)
        origXOffset = grid.xOffset
        origYOffset = grid.yOffset
      }
      case _ => ()
    }
    case MouseDragged(_, pt, _) => pending match {
      case 'r' => drag(pt)
      case 'm' => move(pt)
      case _ => ()
    }
    case MouseReleased(_, pt, _, _, _) => pending match {
      case 'r' =>
        drag(pt)
        resetPending()
      case 'm' =>
        move(pt)
        resetPending()
      case _ => ()
    }
    case _ => ()
  }
  def cursorPaint = SelectedPositionManager.blueGrayPaint
}
