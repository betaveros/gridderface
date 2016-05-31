package gridderface

import scala.swing.event._
import java.awt.{ Graphics2D, Point }
import java.awt.geom.Point2D

class GridderfaceGridSettingMode(val model: GriddableModel,
  private var _activeIndex: Int,
  private var _masterGrid: SimpleGrid,
  private var _rowCount: Int, private var _colCount: Int,
  viewed2grid: Point => Point2D) extends GridderfaceMode with GridProvider with Griddable {

  listenTo(model)
  reactions += {
    case StatusChanged(g) if g != this => publish(StatusChanged(this))
  }

  val name = "Grid"
  def grid: SimpleGrid = _masterGrid
  def grid_=(g: SimpleGrid): Unit = {
    _masterGrid = g
    publish(GridChanged())
    publish(StatusChanged(this))
  }
  def currentGrid: SimpleGrid = model.currentGridOverride getOrElse grid
  def currentGrid_=(g: SimpleGrid): Unit = {
    model.currentGridOverride match {
      case Some(gg) => {
        if (gg equals g) return
        model.currentGridOverride = Some(g)
      }
      case None => {
        if (_masterGrid equals g) return
        _masterGrid = g
      }
    }
    publish(GridChanged())
    publish(StatusChanged(this))
  }
  val currentProvider = new GridProvider {
    def grid = currentGrid
  }
  def moveGrid(xd: Int, yd: Int){
    currentGrid = currentGrid.offsetBy(xd * gridMultiplier, yd * gridMultiplier)
  }
  def adjustGridSize(xm: Int, ym: Int) {
    currentGrid = currentGrid.gridSizeAdjustedBy(gridMultiplier * xm, gridMultiplier * ym)
  }
  def adjustExcess(xm: Int, ym: Int) {
    currentGrid = currentGrid.excessAdjustedBy(gridMultiplier * xm, gridMultiplier * ym)
  }
  def snap() {
    currentGrid = currentGrid.offsetRounded
  }
  def snapAll() {
    currentGrid = currentGrid.allRounded
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
      case KeyTypedData('<') => adjustExcess(-1, -1)
      case KeyTypedData('>') => adjustExcess( 1,  1)
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
      case KeyTypedData('d') => currentGrid = SimpleGrid.defaultGrid; resetPending()
      case KeyTypedData('g') => currentGrid = SimpleGrid.generationGrid; resetPending()
      case KeyTypedData('\u001b' /*esc*/) => resetPending()
  }
  val pendingMoveGridReactions: PartialFunction[KeyData, Unit] = {
      case KeyTypedData('r') => currentGrid = currentGrid.withOffset(0, 0); resetPending()
      case KeyTypedData('\u001b' /*esc*/) => resetPending()
  }
  val singlyResizeGridReactions = KeyDataCombinations.keyDataShiftRCFunction(adjustGridSize)
  def normalStatus = {
    val dimPart = "%.2fx%.2f".format(currentGrid.colWidth, currentGrid.rowHeight)
    val modelPart = " " + model.status
    val expPart = " (M2^%d%s)".format(gridExponent, if (negateFlag) "`" else "")
    val excPart = if (currentGrid.xExcess == 0 && currentGrid.yExcess == 0) {
        ""
      } else {
        " +[%.2fx%.2f]".format(currentGrid.xExcess, currentGrid.yExcess)
      }
    dimPart + modelPart + expPart + excPart
  }
  def status = pending match {
    case ' ' => normalStatus
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
  override def handleColonCommand(command: String, args: Array[String]) = command match {
    case "reset" => {
      grid = SimpleGrid.defaultGrid
      Success("Reset grid")
    }
    case "detach" => {
      model.currentGridOverride = Some(_masterGrid)
      Success("Detached grid")
    }
    case "attach" => {
      model.currentGridOverride = None
      Success("Attached grid")
    }
    case c => Failed("Unrecognized command: " + c)
  }
  var startPt: Point2D = new Point2D.Double(0, 0)
  var origXOffset: Double = 0.0
  var origYOffset: Double = 0.0
  private def drag(pt: Point) = {
    val endPt = viewed2grid(pt)
    val lx = startPt.getX min endPt.getX
    val ly = startPt.getY min endPt.getY
    val hx = startPt.getX max endPt.getX
    val hy = startPt.getY max endPt.getY
    currentGrid = new SimpleGrid(
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
    currentGrid = new SimpleGrid(
      currentGrid.rowHeight,
      currentGrid.colWidth,
      origXOffset + endPt.getX - startPt.getX,
      origYOffset + endPt.getY - startPt.getY)
    publish(GridChanged())
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = _ match {
    case MousePressed(_, pt, _, _, _) => pending match {
      case 'r' => startPt = viewed2grid(pt)
      case 'm' => {
        startPt = viewed2grid(pt)
        origXOffset = currentGrid.xOffset
        origYOffset = currentGrid.yOffset
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
