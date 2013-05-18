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
  private var paint: Paint = Color.BLACK
  private var paintName: String = "Black"
  private var _lockedToCells = false
  def putRectStamp(cpos: CellPosition, st: RectStamp) = {
    val fgContent = new RectStampContent(st, paint)
    putter.putCell(cpos, fgContent)
  }
  def lockedToCells = _lockedToCells
  def lockedToCells_=(b: Boolean) = {
    _lockedToCells = b
    ensureLock()
  }
  def ensureLock() {
    if (_lockedToCells) sel.selected = sel.selected map (_.roundToCell)
  }
    
  def putLineStamp(epos: EdgePosition, st: LineStamp) =
    putter.putEdge(epos, new LineStampContent(st, paint))
  def putPointStamp(ipos: IntersectionPosition, st: PointStamp) =
    putter.putIntersection(ipos, new PointStampContent(st, paint))

  def putClearRectStampAtSelected() = {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => putter.putCell(cpos, new RectStampContent(ClearStamp, paint))
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
  def status = paintName
  def putStampSet(s: StampSet) = putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
  def moveSelected(rd: Int, cd: Int) = {
    val mult = if (lockedToCells) 2 else 1
    sel.selected = sel.selected map (_.deltaPosition(mult*rd, mult*cd))
    ensureLock()
  }
  val cellMoveStamp = new TransverseLineStamp(Strokes.normalStroke)
  val intersectionMoveStamp = Strokes.normalStamp
  def moveAndDrawSelected(rd: Int, cd: Int) = {
    sel.selected foreach (se => {
      val dpos = se.deltaPosition(rd, cd)
      se match {
        case cpos: CellPosition => {
          dpos match {
            case depos: EdgePosition => putLineStamp(depos, cellMoveStamp)
            case _ => throw new AssertionError("moveAndDrawSelected: cell to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case ipos: IntersectionPosition => {
          dpos match {
            case depos: EdgePosition => putLineStamp(depos, intersectionMoveStamp)
            case _ => throw new AssertionError("moveAndDrawSelected: intersection to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case epos: EdgePosition => { 
          dpos match {
            case dcpos: CellPosition => putLineStamp(epos, cellMoveStamp)
            case dipos: IntersectionPosition => putLineStamp(epos, intersectionMoveStamp)
            case _ => throw new AssertionError("moveAndDrawSelected: edge to edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(rd, cd))
        }
      }
    })
    ensureLock()
  }
  private def truePF[B](pf: PartialFunction[KeyData, B]): PartialFunction[List[KeyData], Boolean] =
    new SingletonListPartialFunction(pf andThen {u: B => true})
  val moveReactions = truePF(KeyDataCombinations.keyDataRCFunction(moveSelected))
  val moveAndDrawReactions = truePF(KeyDataCombinations.keyDataShiftRCFunction(moveAndDrawSelected))

  private val globalMap: Map[KeyData, Char] = HashMap(
    KeyTypedData('%') -> '%')
  private val cellMap: Map[KeyData, Char] = HashMap(
    KeyTypedData('=') -> '=',
    KeyTypedData(';') -> ';',
    KeyTypedData('^') -> '^',
    KeyTypedData('_') -> '_',
    KeyTypedData('&') -> '&')
  def commandStartReactions = truePF(
    (sel.selected match {
      // bugnote: "case _: Some[CellPosition]" is too lax, I think due to type erasure
      case Some(CellPosition(_,_)) => cellMap orElse globalMap
      case _ => globalMap
    }) andThen commandStarter
  )
  def paintReactions: PartialFunction[List[KeyData], Boolean] = kd => kd match {
    case List(KeyTypedData('c')) => false
    case List(KeyTypedData('c'), d2) => {
      // TODO: this fails silently
      (PaintSet.defaultMap andThen setPaintSet) lift d2
      true
    }
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
    paint = ps.paint
    paintName = ps.name
    publish(StatusChanged(this))
  }
  def keyListReactions = (moveReactions
    orElse moveAndDrawReactions
    orElse commandStartReactions
    orElse truePF(StampSet.defaultMap andThen putStampSet)
    orElse paintReactions)
  def selectNear(pt: Point) {
    sel.selected = Some(point2pos(pt))
    ensureLock()
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => selectNear(pt)
    case MouseClicked(_, pt, _, _, _) => selectNear(pt)
  }
}
