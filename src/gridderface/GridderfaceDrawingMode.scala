package gridderface

import java.awt.{ Paint, Color }
import scala.collection.immutable.HashMap
import gridderface.stamp.{ RectStamp, LineStamp, PointStamp, TextRectStamp }
import scala.swing.event.{ MouseEvent, MouseClicked }
import scala.swing.event.MousePressed

class GridderfaceDrawingMode(sel: SelectedPositionManager, putter: ContentPutter, point2pos: java.awt.Point => Position) extends GridderfaceMode {
  val name = "Draw"
  private var paint: Paint = Color.BLACK
  private var paintName: String = "Black"
  def putRectStamp(cpos: CellPosition, st: RectStamp) =
    putter.putCell(cpos, new RectStampContent(st, paint))
  def putLineStamp(epos: EdgePosition, st: LineStamp) =
    putter.putEdge(epos, new LineStampContent(st, paint))
  def putPointStamp(ipos: IntersectionPosition, st: PointStamp) =
    putter.putIntersection(ipos, new PointStampContent(st, paint))

  def putStamp(rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => rectStamp foreach (putRectStamp(cpos, _))
      case epos: EdgePosition => lineStamp foreach (putLineStamp(epos, _))
      case ipos: IntersectionPosition => pointStamp foreach (putPointStamp(ipos, _))
    })
  }
  def status = paintName
  def putStampSet(s: StampSet) = putStamp(s.rectStamp, s.lineStamp, s.pointStamp)
  val moveReactions = KeyDataCombinations.keyDataRCFunction(sel.moveSelected)

  private val cellMap: Map[KeyData, Char] = HashMap(KeyTypedData('=') -> '=', KeyTypedData(';') -> ';')
  def commandPrefixMap: Map[KeyData, Char] = {
    sel.selected match {
      // bugnote: "case _: Some[CellPosition]" is too lax, I think due to type erasure
      case Some(CellPosition(_,_)) => cellMap
      case _ => Map.empty
    }
  }
  def handleCommand(prefix: Char, str: String) = prefix match {
    case '=' =>
      putStamp(Some(new TextRectStamp(str))); Right("You put " + str)
    case ';' =>
      putStamp(Some(new TextRectStamp(str, TextRectStamp.smallFont))); Right("You put " + str)
    case c => Left("Drawing Mode can't handle this prefix: " + c)
  }
  def setPaintSet(ps: PaintSet) {
    paint = ps.paint
    paintName = ps.name
    publish(StatusChanged(this))
  }

  val keyReactions = (moveReactions
    orElse (StampSet.defaultMap andThen putStampSet)
    orElse (PaintSet.defaultMap andThen setPaintSet))
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => sel.selected = Some(point2pos(pt))
    case MouseClicked(_, pt, _, _, _) => sel.selected = Some(point2pos(pt))
  }
}