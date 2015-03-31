package gridderface

import java.awt.{ Paint, Color, Point } // must avoid java.awt.List
import scala.collection.immutable.HashMap
import scala.swing.event._
import gridderface.stamp._

class GridderfaceDrawingMode(val name: String, sel: SelectedPositionManager,
  gridLists: List[GriddablePositionMapList],
  gridListNames: List[Option[String]],
  private var _currentListIndex: Int,
  point2pos: java.awt.Point => Position, commandStarter: Char => Unit) extends GridderfaceMode {

  private var cellPaint: Paint = Color.BLACK
  private var cellPaintName: String = "Black"
  private var edgePaint: Paint = Color.BLACK
  private var edgePaintName: String = "Black"
  private var intersectionPaint: Paint = Color.BLACK
  private var intersectionPaintName: String = "Black"
  private var writeSet: WriteSet = WriteSet.writeSet
  private var lastStampSet: Option[StampSet] = None
  private var _paintStatus: String = "Black"
  private var _drawStatus: String = ""
  private var _lockFunction: Position => Position = identity[Position]
  private var _lockMultiplier = 1
  private var _gridList = gridLists(_currentListIndex)
  def putRectStamp(cpos: CellPosition, st: RectStamp) = {
    val fgContent = new RectStampContent(st, cellPaint)
    _gridList.putCell(cpos, fgContent)
  }
  def ensureLock() {
    sel.selected = sel.selected map _lockFunction
  }
  private def setLockFunction(f: Position => Position) = {
    _lockFunction = f
    ensureLock()
  }
  def lockToCells() = {
    setLockFunction(_.roundToCell)
    _lockMultiplier = 2
  }
  def lockToIntersections() = {
    setLockFunction(_.roundToIntersection)
    _lockMultiplier = 2
  }
  def unlock() = {
    setLockFunction(identity[Position])
    _lockMultiplier = 1
  }
  def putLineStamp(epos: EdgePosition, st: LineStamp) =
    _gridList.putEdge(epos, new LineStampContent(st, edgePaint))
  def putPointStamp(ipos: IntersectionPosition, st: PointStamp) =
    _gridList.putIntersection(ipos, new PointStampContent(st, intersectionPaint))

  def putClearRectStampAtSelected() = {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => _gridList.putCell(cpos, new RectStampContent(ClearStamp, cellPaint))
      case epos: EdgePosition => putLineStamp(epos, ClearStamp)
      case ipos: IntersectionPosition => putPointStamp(ipos, ClearStamp)
    })
  }
  def putStampAtPosition(pos: Position, rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    pos match {
      case cpos: CellPosition => rectStamp foreach (putRectStamp(cpos, _))
      case epos: EdgePosition => lineStamp foreach (putLineStamp(epos, _))
      case ipos: IntersectionPosition => pointStamp foreach (putPointStamp(ipos, _))
    }
  }
  def putStampAtSelected(rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    sel.selected foreach (pos => putStampAtPosition(pos, rectStamp, lineStamp,
      pointStamp))
  }
  def putContentAtSelected(rectContent: Option[RectContent] = None,
    lineContent: Option[LineContent] = None,
    pointContent: Option[PointContent] = None) {
    sel.selected foreach (_ match {
      case cpos: CellPosition => rectContent foreach (_gridList.putCell(cpos, _))
      case epos: EdgePosition => lineContent foreach (_gridList.putEdge(epos, _))
      case ipos: IntersectionPosition => pointContent foreach (_gridList.putIntersection(ipos, _))
    })
  }
  def status = _drawStatus ++ _paintStatus ++ " | " ++ (gridListNames(_currentListIndex) match {
      case None => ""
      case Some(s) => "(" ++ s ++ ")"
    }) ++ _gridList.status
  def putStampSet(s: StampSet): Unit = {
    lastStampSet = Some(s)
    putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
  }
  def putContentSet(s: ContentSet): Unit = {
    putContentAtSelected(s.rectContent, s.lineContent, s.pointContent)
  }
  def moveSelected(rd: Int, cd: Int) = {
    val mult = _lockMultiplier
    sel.selected = sel.selected map (_.deltaPosition(mult*rd, mult*cd))
    ensureLock()
  }
  def moveAndDrawSelected(rd: Int, cd: Int) = {
    sel.selected foreach (se => {
      val dpos = se.deltaPosition(rd, cd)
      se match {
        case cpos: CellPosition => {
          dpos match {
            case depos: EdgePosition => writeSet.cellWriteStamp foreach {s => putLineStamp(depos, s)}
            case _ => throw new AssertionError("moveAndDrawSelected: cell to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case ipos: IntersectionPosition => {
          dpos match {
            case depos: EdgePosition => writeSet.intersectionWriteStamp foreach {s => putLineStamp(depos, s)}
            case _ => throw new AssertionError("moveAndDrawSelected: intersection to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case epos: EdgePosition => {
          dpos match {
            case dcpos: CellPosition => writeSet.cellWriteStamp foreach {s => putLineStamp(epos, s)}
            case dipos: IntersectionPosition => writeSet.intersectionWriteStamp foreach {s => putLineStamp(epos, s)}
            case _ => throw new AssertionError("moveAndDrawSelected: edge to edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(rd, cd))
        }
      }
    })
    ensureLock()
  }
  private def completePF[B](pf: PartialFunction[KeyData, B]): PartialFunction[List[KeyData], KeyResult] =
    new SingletonListPartialFunction(pf andThen {u: B => KeyComplete})
  val moveReactions = completePF(KeyDataCombinations.keyDataRCFunction(moveSelected))
  val moveAndDrawReactions = completePF(KeyDataCombinations.keyDataShiftRCFunction(moveAndDrawSelected))

  private val globalMap: Map[KeyData, Char] = HashMap(
    KeyTypedData('%') -> '%')
  private val cellMap: Map[KeyData, Char] = HashMap(
    KeyTypedData('=') -> '=',
    KeyTypedData(';') -> ';',
    KeyTypedData('^') -> '^',
    KeyTypedData('_') -> '_',
    KeyTypedData('&') -> '&',
    KeyTypedData('#') -> '#')
  def commandStartReactions = completePF(
    (sel.selected match {
      // bugnote: "case _: Some[CellPosition]" is too lax, I think due to type erasure
      case Some(CellPosition(_,_)) => cellMap orElse globalMap
      case _ => globalMap
    }) andThen commandStarter
  )
  private def unitComplete(s: Option[Unit]) = s match {
    case Some(()) => KeyComplete
    case None => KeyUndefined
  }
  def paintReactions: PartialFunction[List[KeyData], KeyResult] = kd => kd match {
    case List(KeyTypedData('c')) => KeyIncomplete
    case List(KeyTypedData('c'), d2) =>
      unitComplete(PaintSet.defaultMap andThen setPaintSet lift d2)

    case List(KeyTypedData('C')) => KeyIncomplete
    case List(KeyTypedData('C'), KeyTypedData('c')) => KeyIncomplete
    case List(KeyTypedData('C'), KeyTypedData('c'), d2) =>
      unitComplete(PaintSet.defaultMap andThen setCellPaintSet lift d2)
    case List(KeyTypedData('C'), KeyTypedData('e')) => KeyIncomplete
    case List(KeyTypedData('C'), KeyTypedData('e'), d2) =>
      unitComplete(PaintSet.defaultMap andThen setEdgePaintSet lift d2)
    case List(KeyTypedData('C'), KeyTypedData('i')) => KeyIncomplete
    case List(KeyTypedData('C'), KeyTypedData('i'), d2) =>
      unitComplete(PaintSet.defaultMap andThen setIntersectionPaintSet lift d2)
  }
  def writeReactions: PartialFunction[List[KeyData], KeyResult] = kd => kd match {
    case List(KeyTypedData('w')) => KeyIncomplete
    case List(KeyTypedData('w'), d2) =>
      unitComplete(WriteSet.defaultMap andThen setWriteSet lift d2)
  }
  def gridListReactions: PartialFunction[List[KeyData], KeyResult] = kd => kd match {
    case List(KeyPressedData(Key.Tab, 0)) => {
      _gridList.selectNextGrid()
      publish(StatusChanged(this))
      KeyCompleteWith(Success("Selected layer " ++ _gridList.status))
    }
    case List(KeyPressedData(Key.Tab, Key.Modifier.Shift)) => {
      _gridList.selectNextGrid()
      publish(StatusChanged(this))
      KeyCompleteWith(Success("Selected layer " ++ _gridList.status))
    }
    case List(KeyPressedData(Key.Tab, Key.Modifier.Control)) => {
      _currentListIndex = (_currentListIndex + 1) % gridLists.length
      _gridList = gridLists(_currentListIndex)
      publish(StatusChanged(this))
      KeyCompleteWith(Success("Selected layer list"))
    }
  }
  def handleCommand(prefix: Char, str: String) = prefix match {
    case '=' =>
      putStampAtSelected(Some(new OneTextRectStamp(str))); Success("You put " + str)
    case ';' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small))); Success("You put " + str)
    case '^' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small, 0.125f, 0f))); Success("You put " + str)
    case '_' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small, 0.125f, 1f))); Success("You put " + str)
    case '%' =>
      for (ps <- GridderfaceStringParser.parseColorString(str)) yield {
        setPaintSet(ps); "Set color to " ++ ps.name
      }
    case '&' => {
      val tokens = str.split("\\s+")
      tokens.length match {
        case 2 => putStampAtSelected(Some(new TwoTextRectStamp(tokens(0), tokens(1)))); Success("")
        case 3 => putStampAtSelected(Some(new ThreeTextRectStamp(tokens(0), tokens(1), tokens(2)))); Success("")
        case 4 => putStampAtSelected(Some(new FourTextRectStamp(tokens(0), tokens(1), tokens(2), tokens(3)))); Success("")
        case _ => Failed("Wrong number of tokens for &")
      }
    }
    case '#' =>
      putStampAtSelected(Some(SudokuPencilRectStamp.createSudokuPencilRectStampFromString(str))); Success("")
  }
  def addLayer(): Status[String] = {
    _gridList.addGrid(); publish(StatusChanged(this))
    Success("New layer added")
  }
  def removeLayer(): Status[String] = {
    _gridList.removeGrid(); publish(StatusChanged(this))
    Success("Current layer removed")
  }
  def removeAll(): Status[String] = {
    _gridList.removeAll(); publish(StatusChanged(this))
    Success("All layers removed")
  }
  override def handleColonCommand(command: String, args: Array[String]) = command match {
    case "lock"   => lockToCells(); Success("Locked to cells")
    case "ilock"  => lockToIntersections(); Success("Locked to intersections")
    case "unlock" => unlock(); Success("Unlocked")

    case "default" => _drawStatus = ""; _drawReactions = defaultDrawReactions; publish(StatusChanged(this)); Success("Default")
    case "alpha"   => _drawStatus = "[alpha]"; _drawReactions = alphaDrawReactions; publish(StatusChanged(this)); Success("Alpha")
    case "fill"    => _drawStatus = "[fill]"; _drawReactions = fillDrawReactions; publish(StatusChanged(this)); Success("Fill")

    case "newlayer" => addLayer()
    case "addlayer" => addLayer()
    case "rmlayer"  => removeLayer()
    case "dellayer" => removeLayer()
    case "rmall"    => removeAll()
    case "delall"   => removeAll()
    case "clear" =>
      _gridList.clearGrid(); publish(StatusChanged(this))
      Success("Content cleared")
    case "clearall" =>
      _gridList.clearAll(); publish(StatusChanged(this))
      Success("All content cleared")
    case c => Failed("Unrecognized command: " + c)
  }
  def setPaintSet(ps: PaintSet) {
    cellPaint = ps.paint
    cellPaintName = ps.name
    edgePaint = ps.paint
    edgePaintName = ps.name
    intersectionPaint = ps.paint
    intersectionPaintName = ps.name
    _paintStatus = ps.name
    publish(StatusChanged(this))
  }
  def createPaintStatus() {
    _paintStatus = cellPaintName ++ " / " ++ edgePaintName ++ " / " ++ intersectionPaintName
  }
  def setCellPaintSet(ps: PaintSet) {
    cellPaint = ps.paint
    cellPaintName = ps.name
    createPaintStatus()
    publish(StatusChanged(this))
  }
  def setEdgePaintSet(ps: PaintSet) {
    edgePaint = ps.paint
    edgePaintName = ps.name
    createPaintStatus()
    publish(StatusChanged(this))
  }
  def setIntersectionPaintSet(ps: PaintSet) {
    intersectionPaint = ps.paint
    intersectionPaintName = ps.name
    createPaintStatus()
    publish(StatusChanged(this))
  }
  def setWriteSet(ws: WriteSet) {
    writeSet = ws
    // publish(StatusChanged(this))
  }
  val defaultDrawReactions = completePF(StampSet.defaultMap andThen putStampSet)
  val alphaDrawReactions = completePF(StampSet.alphaMap andThen putStampSet)
  val fillDrawReactions = completePF(ContentSet.fillMap andThen putContentSet)
  private var _drawReactions = defaultDrawReactions

  def keyListReactions = (moveReactions
    orElse commandStartReactions
    orElse _drawReactions
    orElse moveAndDrawReactions
    orElse paintReactions
    orElse writeReactions
    orElse gridListReactions)
  def selectNear(pt: Point) {
    sel.selected = Some(point2pos(pt))
    ensureLock()
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => selectNear(pt)
    case MouseClicked(_, pt, _, _, _) => selectNear(pt)
  }
  def cursorPaint = SelectedPositionManager.greenPaint
}
