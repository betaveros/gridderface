package gridderface

import java.awt.{ Paint, Color }
import scala.collection.immutable.HashMap
import gridderface.stamp.{ RectStamp, LineStamp, PointStamp, TextRectStamp }
import scala.swing.event.{ MouseEvent, MouseClicked }
import scala.swing.event.MousePressed
import gridderface.stamp.FillRectStamp
import gridderface.stamp.ClearStamp

class GridderfaceDrawingMode(sel: SelectedPositionManager, putter: ContentPutter, point2pos: java.awt.Point => Position) extends GridderfaceMode {
  val name = "Draw"
  private var paint: Paint = Color.BLACK
  private var paintName: String = "Black"
  private var backgroundPaint: Option[Paint] = None
  private var backgroundPaintName: String = "-"
  private var waitingForBackground = false
  def putRectStamp(cpos: CellPosition, st: RectStamp) = {
    val fgContent = new RectStampContent(st, paint)
    backgroundPaint match {
      case None => putter.putCell(cpos, fgContent)
      case Some(paint) => putter.putCell(cpos, CombinedRectContent(
          new RectStampContent(FillRectStamp, paint), fgContent))
    }
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
  def status = "%s / %s".format(paintName, 
      if (waitingForBackground) "?" else backgroundPaintName)
  def putStampSet(s: StampSet) = putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
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
      putStampAtSelected(Some(new TextRectStamp(str))); Success("You put " + str)
    case ';' =>
      putStampAtSelected(Some(new TextRectStamp(str, TextRectStamp.smallFont))); Success("You put " + str)
    case c => Failed("Drawing Mode can't handle this prefix: " + c)
  }
  def setPaintSet(ps: PaintSet) {
    paint = ps.paint
    paintName = ps.name
    publish(StatusChanged(this))
  }
  def setBackgroundPaintSet(ps: PaintSet) {
    backgroundPaint = Some(ps.paint)
    backgroundPaintName = ps.name
    waitingForBackground = false
    publish(StatusChanged(this))
  }
  def clearBackgroundPaintSet() {
    backgroundPaint = None
    backgroundPaintName = "-"
    waitingForBackground = false
    publish(StatusChanged(this))
  }
  val backgroundReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData('`') => waitingForBackground = true; publish(StatusChanged(this))
  }
  val backgroundClearReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData(' ') => clearBackgroundPaintSet()
  }
  val clearContentReactions: PartialFunction[KeyData, Unit] = {
    // override background, if any
    case KeyTypedData(' ') => putClearRectStampAtSelected()
  }
  def keyReactions = if (waitingForBackground) {
    (PaintSet.defaultMap andThen setBackgroundPaintSet) orElse
    backgroundClearReactions
  } else {
    (moveReactions
    orElse clearContentReactions
    orElse (StampSet.defaultMap andThen putStampSet)
    orElse (PaintSet.defaultMap andThen setPaintSet)
    orElse backgroundReactions)
  }
  val mouseReactions: PartialFunction[MouseEvent, Unit] = event => event match {
    case MousePressed(_, pt, _, _, _) => sel.selected = Some(point2pos(pt))
    case MouseClicked(_, pt, _, _, _) => sel.selected = Some(point2pos(pt))
  }
}