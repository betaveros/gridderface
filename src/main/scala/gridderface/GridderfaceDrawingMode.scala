package gridderface

import java.awt.{ Paint, Color }
import scala.collection.immutable.HashMap
import scala.swing.event.{ MouseEvent, MouseClicked }
import scala.swing.event.MousePressed
import java.awt.Point
import gridderface.stamp._

class GridderfaceDrawingMode(sel: SelectedPositionManager, putter: ContentPutter,
  point2pos: java.awt.Point => Position, commandStarter: Char => Unit) extends GridderfaceMode {
  val name = "Draw"
  private var cellPaint: Paint = Color.BLACK
  private var cellPaintName: String = "Black"
  private var edgePaint: Paint = Color.BLACK
  private var edgePaintName: String = "Black"
  private var intersectionPaint: Paint = Color.BLACK
  private var intersectionPaintName: String = "Black"
  private var writeSet: WriteSet = WriteSet.writeSet
  private var lastStampSet: Option[StampSet] = None
  private var _status: String = "Black"
  private var _lockFunction: Position => Position = identity[Position]
  private var _lockMultiplier = 1
  def putRectStamp(cpos: CellPosition, st: RectStamp) = {
    val fgContent = new RectStampContent(st, cellPaint)
    putter.putCell(cpos, fgContent)
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
    putter.putEdge(epos, new LineStampContent(st, edgePaint))
  def putPointStamp(ipos: IntersectionPosition, st: PointStamp) =
    putter.putIntersection(ipos, new PointStampContent(st, intersectionPaint))

  def putClearRectStampAtSelected() = {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => putter.putCell(cpos, new RectStampContent(ClearStamp, cellPaint))
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
  def status = _status
  def putStampSet(s: StampSet): Unit = {
    lastStampSet = Some(s)
    putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
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
    KeyTypedData('&') -> '&')
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
  def handleCommand(prefix: Char, str: String) = prefix match {
    case '=' =>
      putStampAtSelected(Some(new TextRectStamp(str))); Success("You put " + str)
    case ';' =>
      putStampAtSelected(Some(new TextRectStamp(str, TextRectStamp.smallFont))); Success("You put " + str)
    case '^' =>
      putStampAtSelected(Some(new TextRectStamp(str, TextRectStamp.smallFont, 0.125f, 0f))); Success("You put " + str)
    case '_' =>
      putStampAtSelected(Some(new TextRectStamp(str, TextRectStamp.smallFont, 0.125f, 1f))); Success("You put " + str)
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
    case c => Failed("Drawing Mode can't handle this prefix: " + c)
  }
  def setPaintSet(ps: PaintSet) {
    cellPaint = ps.paint
    cellPaintName = ps.name
    edgePaint = ps.paint
    edgePaintName = ps.name
    intersectionPaint = ps.paint
    intersectionPaintName = ps.name
    _status = ps.name
    publish(StatusChanged(this))
  }
  def createStatus() {
    _status = cellPaintName ++ " / " ++ edgePaintName ++ " / " ++ intersectionPaintName
  }
  def setCellPaintSet(ps: PaintSet) {
    cellPaint = ps.paint
    cellPaintName = ps.name
    createStatus()
    publish(StatusChanged(this))
  }
  def setEdgePaintSet(ps: PaintSet) {
    edgePaint = ps.paint
    edgePaintName = ps.name
    createStatus()
    publish(StatusChanged(this))
  }
  def setIntersectionPaintSet(ps: PaintSet) {
    intersectionPaint = ps.paint
    intersectionPaintName = ps.name
    createStatus()
    publish(StatusChanged(this))
  }
  def setWriteSet(ws: WriteSet) {
    writeSet = ws
    // publish(StatusChanged(this))
  }
  def keyListReactions = (moveReactions
    orElse moveAndDrawReactions
    orElse commandStartReactions
    orElse completePF(StampSet.defaultMap andThen putStampSet)
    orElse paintReactions
    orElse writeReactions)
  def selectNear(pt: Point) {
    sel.selected = Some(point2pos(pt))
    ensureLock()
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => selectNear(pt)
    case MouseClicked(_, pt, _, _, _) => selectNear(pt)
  }
}
