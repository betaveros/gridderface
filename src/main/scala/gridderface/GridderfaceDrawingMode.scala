package gridderface

import java.awt.{ Paint, Color }
import scala.collection.immutable.HashMap
import gridderface.stamp.{ RectStamp, LineStamp, PointStamp, TextRectStamp, TwoTextRectStamp, ThreeTextRectStamp, FourTextRectStamp }
import scala.swing.event.{ MouseEvent, MouseClicked }
import scala.swing.event.MousePressed
import gridderface.stamp.FillRectStamp
import gridderface.stamp.ClearStamp
import java.awt.Point

class GridderfaceDrawingMode(sel: SelectedPositionManager, putter: ContentPutter, point2pos: java.awt.Point => Position) extends GridderfaceMode {
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
  def putStampAtSelected(rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => rectStamp foreach (putRectStamp(cpos, _))
      case epos: EdgePosition => lineStamp foreach (putLineStamp(epos, _))
      case ipos: IntersectionPosition => pointStamp foreach (putPointStamp(ipos, _))
    })
  }
  def status = paintName
  def putStampSet(s: StampSet) = putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
  def moveSelected(rd: Int, cd: Int) = {
    val mult = if (lockedToCells) 2 else 1
    sel.selected = sel.selected map (_.deltaPosition(mult*rd, mult*cd))
    ensureLock()
  }
  val moveReactions = KeyDataCombinations.keyDataRCFunction(moveSelected)

  private val cellMap: Map[KeyData, Char] = HashMap(
    KeyTypedData('=') -> '=',
    KeyTypedData(';') -> ';',
    KeyTypedData('^') -> '^',
    KeyTypedData('_') -> '_',
    KeyTypedData('&') -> '&')
  def commandPrefixMap: Map[KeyData, Char] = {
    sel.selected match {
      // bugnote: "case _: Some[CellPosition]" is too lax, I think due to type erasure
      case Some(CellPosition(_,_)) => cellMap
      case _ => Map.empty
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
  def keyReactions = (moveReactions
    orElse (StampSet.defaultMap andThen putStampSet)
    orElse (PaintSet.defaultMap andThen setPaintSet))
  def selectNear(pt: Point) {
    sel.selected = Some(point2pos(pt))
    ensureLock()
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => selectNear(pt)
    case MouseClicked(_, pt, _, _, _) => selectNear(pt)
  }
}
