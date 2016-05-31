package gridderface

import java.awt.{ Paint, Color, Point } // must avoid java.awt.List
import scala.collection.immutable.HashMap
import scala.swing.event._
import gridderface.stamp._

class GridderfaceDrawingMode(val name: String, sel: SelectedPositionManager,
  gridModels: List[GriddableModel],
  gridModelNames: List[Option[String]],
  private var _currentModelIndex: Int,
  point2pos: java.awt.Point => Position, commandStarter: Char => Unit) extends GridderfaceMode {

  gridModels.foreach(listenTo(_))
  reactions += {
    case StatusChanged(g) if g != this => publish(StatusChanged(this))
  }

  private var cellPaint: Paint = Color.BLACK
  private var cellPaintName: String = "Black"
  private var edgePaint: Paint = Color.BLACK
  private var edgePaintName: String = "Black"
  private var intersectionPaint: Paint = Color.BLACK
  private var intersectionPaintName: String = "Black"
  private var writeSet: WriteSet = WriteSet.writeSet
  private var lastRectStamp: Option[RectStamp] = None
  private var lastLineStamp: Option[LineStamp] = None
  private var lastPointStamp: Option[PointStamp] = None
  private var _paintStatus: String = "Black"
  private var _drawStatus: String = ""
  private var _lockFunction: Position => Position = identity[Position]
  private var _lockMultiplier = 1
  private var _advanceEnabled = false
  private var _advanceRowDelta = 0
  private var _advanceColDelta = 2
  private var _gridModel = gridModels(_currentModelIndex)
  def putRectStamp(cpos: CellPosition, st: RectStamp) = {
    val fgContent = new RectStampContent(st, cellPaint)
    _gridModel.putCell(cpos, fgContent)
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
  def refreshSelectedStamp(): Unit = {
    if (_advanceEnabled) {
      if (_advanceColDelta == 0) {
        sel.cellStamp = SelectedPositionManager.downPointingStamp
      } else {
        sel.cellStamp = SelectedPositionManager.rightPointingStamp
      }
    } else {
      sel.cellStamp = SelectedPositionManager.outlineStamp
    }
  }
  def advanceEnabled = _advanceEnabled
  def advanceEnabled_=(e: Boolean): Unit = {
    _advanceEnabled = e
    refreshSelectedStamp()
  }
  def setAdvanceDelta(rd: Int, cd: Int) {
    _advanceRowDelta = rd
    _advanceColDelta = cd
    refreshSelectedStamp()
  }
  def putLineStamp(epos: EdgePosition, st: LineStamp) =
    _gridModel.putEdge(epos, new LineStampContent(st, edgePaint))
  def putPointStamp(ipos: IntersectionPosition, st: PointStamp) =
    _gridModel.putIntersection(ipos, new PointStampContent(st, intersectionPaint))

  def putClearRectStampAtSelected() = {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => _gridModel.putCell(cpos, new RectStampContent(ClearStamp, cellPaint))
      case epos: EdgePosition => putLineStamp(epos, ClearStamp)
      case ipos: IntersectionPosition => putPointStamp(ipos, ClearStamp)
    })
    advanceSelected()
  }
  def backspace() = {
    sel.selected foreach (se => se match {
      case cpos: CellPosition => _gridModel.putCell(cpos, new RectStampContent(ClearStamp, cellPaint))
      case epos: EdgePosition => putLineStamp(epos, ClearStamp)
      case ipos: IntersectionPosition => putPointStamp(ipos, ClearStamp)
    })
    retreatSelected()
  }
  def putStampAtPosition(pos: Position, rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    pos match {
      case cpos: CellPosition => {
        rectStamp foreach (putRectStamp(cpos, _))
        lastRectStamp = rectStamp orElse lastRectStamp
      }
      case epos: EdgePosition => {
        lineStamp foreach (putLineStamp(epos, _))
        lastLineStamp = lineStamp orElse lastLineStamp
      }
      case ipos: IntersectionPosition => {
        pointStamp foreach (putPointStamp(ipos, _))
        lastPointStamp = pointStamp orElse lastPointStamp
      }
    }
  }
  def putStampAtSelected(rectStamp: Option[RectStamp] = None,
    lineStamp: Option[LineStamp] = None,
    pointStamp: Option[PointStamp] = None) {
    sel.selected foreach (pos => putStampAtPosition(pos, rectStamp, lineStamp,
      pointStamp))
    advanceSelected()
  }
  def putContentAtSelected(rectContent: Option[RectContent] = None,
    lineContent: Option[LineContent] = None,
    pointContent: Option[PointContent] = None) {
    sel.selected foreach (_ match {
      case cpos: CellPosition => rectContent foreach (_gridModel.putCell(cpos, _))
      case epos: EdgePosition => lineContent foreach (_gridModel.putEdge(epos, _))
      case ipos: IntersectionPosition => pointContent foreach (_gridModel.putIntersection(ipos, _))
    })
    advanceSelected()
  }
  def status = _drawStatus ++ _paintStatus ++ " | " ++ (gridModelNames(_currentModelIndex) match {
      case None => ""
      case Some(s) => "(" ++ s ++ ")"
    }) ++ _gridModel.status
  def putStampSet(s: StampSet): Unit = {
    putStampAtSelected(s.rectStamp, s.lineStamp, s.pointStamp)
  }
  def putContentSet(s: ContentSet): Unit = {
    putContentAtSelected(s.rectContent, s.lineContent, s.pointContent)
  }
  def moveSelected(rd: Int, cd: Int) = {
    if (!(rd == 0 && cd == 0)) {
      sel.selected = sel.selected map (_.deltaPosition(rd, cd))
      ensureLock()
    }
  }
  def advanceSelected() = {
    if (_advanceEnabled) {
      moveSelected(_advanceRowDelta, _advanceColDelta)
    }
  }
  def retreatSelected() = {
    if (_advanceEnabled) {
      moveSelected(-_advanceRowDelta, -_advanceColDelta)
    }
  }
  def moveSelectedFromInput(rd: Int, cd: Int) = {
    val mult = _lockMultiplier
    moveSelected(mult*rd, mult*cd)
    if (_advanceEnabled) {
      setAdvanceDelta(rd.abs * 2, cd.abs * 2)
    }
  }
  def lineContentify(g: Griddable): Option[LineContent] = g match {
    case EdgeGriddable(c: LineContent, _) => Some(c)
    case _ => None
  }
  def moveAndDrawSelected(rd: Int, cd: Int) = {
    sel.selected foreach (se => {
      val dpos = se.deltaPosition(rd, cd)
      se match {
        case cpos: CellPosition => {
          dpos match {
            case depos: EdgePosition => {
              _gridModel.putEdge(depos, writeSet.cellWrite(edgePaint, _gridModel get depos flatMap lineContentify))
            }
            case _ => throw new AssertionError("moveAndDrawSelected: cell to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case ipos: IntersectionPosition => {
          dpos match {
            case depos: EdgePosition => _gridModel.putEdge(depos, writeSet.intersectionWrite(edgePaint, _gridModel get depos flatMap lineContentify))
            case _ => throw new AssertionError("moveAndDrawSelected: intersection to non-edge")
          }
          sel.selected = sel.selected map (_.deltaPosition(2*rd, 2*cd))
        }
        case epos: EdgePosition => {
          dpos match {
            case dcpos:         CellPosition => _gridModel.putEdge(epos, writeSet.        cellWrite(edgePaint, _gridModel get epos flatMap lineContentify))
            case dipos: IntersectionPosition => _gridModel.putEdge(epos, writeSet.intersectionWrite(edgePaint, _gridModel get epos flatMap lineContentify))
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
  val moveReactions = completePF(KeyDataCombinations.keyDataRCFunction(moveSelectedFromInput))
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
    case List(KeyTypedData('c'), d2) => d2 match {
      case KeyTypedData('d') => KeyIncomplete
      case KeyTypedData('l') => KeyIncomplete
      case _ => unitComplete(PaintSet.defaultMap andThen setPaintSet lift d2)
    }
    case List(KeyTypedData('c'), KeyTypedData('d'), d3) => unitComplete(PaintSet.defaultMap andThen (_.darkerSet) andThen setPaintSet lift d3)
    case List(KeyTypedData('c'), KeyTypedData('l'), d3) => unitComplete(PaintSet.defaultMap andThen (_.lighterSet) andThen setPaintSet lift d3)

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
      _gridModel.selectNextGrid()
      KeyCompleteWith(Success("Selected layer " ++ _gridModel.status))
    }
    case List(KeyPressedData(Key.Tab, Key.Modifier.Shift)) => {
      _gridModel.selectPreviousGrid()
      KeyCompleteWith(Success("Selected layer " ++ _gridModel.status))
    }
    case List(KeyPressedData(Key.Tab, Key.Modifier.Control)) => {
      _currentModelIndex = (_currentModelIndex + 1) % gridModels.length
      _gridModel = gridModels(_currentModelIndex)
      publish(StatusChanged(this))
      KeyCompleteWith(Success("Selected layer list"))
    }
  }
  def putArrow(str: String, rd: Int, cd: Int, arr: ArrowTextArrow, phrase: String): Status[String] = {
    if (str.length == 0) {
      putStampAtSelected(Some(ArrowStamp(rd, cd))); Success("You put " ++ phrase ++ " arrow")
    } else {
      putStampAtSelected(Some(ArrowTextRectStamp(str, arr))); Success("You put " ++ phrase ++ " arrow with text " ++ str)
    }
  }
  def handleCommand(prefix: Char, str: String): Status[String] = prefix match {
    case '=' => {
      if (str.length == 0) return Success("Canceled")
      val start = if (str.length >= 2) str.substring(0, 2) else str.substring(0, 1)
      start match {
        case "-^" => putArrow(str.substring(2),  0, -1,    UpArrow,   "an upward")
        case "-v" => putArrow(str.substring(2),  0,  1,  DownArrow,  "a downward")
        case "<-" => putArrow(str.substring(2), -1,  0,  LeftArrow,  "a leftward")
        case "->" => putArrow(str.substring(2),  1,  0, RightArrow, "a rightward")
        case "_v"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, false, false, false))); Success("You put a quarter triangle")
        case "<|"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, true, false, false))); Success("You put a quarter triangle")
        case "_^"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, false, true, false))); Success("You put a quarter triangle")
        case "|>"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, false, false, true))); Success("You put a quarter triangle")
        case "\\|" => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, true, false, false))); Success("You put a half triangle")
        case "/|"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, true, true, false))); Success("You put a half triangle")
        case "|\\" => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, false, true, true))); Success("You put a half triangle")
        case "|/"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, false, false, true))); Success("You put a half triangle")
        case "><"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, true, false, true))); Success("You put a half triangle")
        case "v^"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, false, true, false))); Success("You put a half triangle")
        case "!v"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, false, true, true, true))); Success("You put a 3/4 triangle complement")
        case "<!"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, false, true, true))); Success("You put a 3/4 triangle complement")
        case "!^"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, true, false, true))); Success("You put a 3/4 triangle complement")
        case "!>"  => putStampAtSelected(Some(QuarterTrianglesRectStamp(1, Fill, true, true, true, false))); Success("You put a 3/4 triangle complement")
        case _ => putStampAtSelected(Some(new OneTextRectStamp(str))); Success("You put " + str)
      }
    }
    case ';' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small))); Success("You put " + str)
    case '^' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small, 0.125f, 0f))); Success("You put " + str)
    case '_' =>
      putStampAtSelected(Some(new OneTextRectStamp(str, OneTextRectStamp.FontSize.Small, 0.125f, 1f))); Success("You put " + str)
    case '%' =>
      for (ps <- PaintStringifier.parsePaintSet(str)) yield {
        setPaintSet(ps); "Set color to " ++ ps.name
      }
    case '&' => {
      val tokens = str.split("\\s+")
      tokens.length match {
        case 2 => putStampAtSelected(Some(new TwoTextRectStamp(tokens(0), tokens(1)))); Success("")
        case 3 => putStampAtSelected(Some(new ThreeTextRectStamp(tokens(0), tokens(1), tokens(2)))); Success("")
        case 4 => putStampAtSelected(Some(new FourTextRectStamp(tokens(0), tokens(1), tokens(2), tokens(3)))); Success("")
        case _ => Failed("wrong number of tokens for &")
      }
    }
    case '#' =>
      putStampAtSelected(Some(SudokuPencilRectStamp.createSudokuPencilRectStampFromString(str))); Success("")
  }
  def removeAll(): Status[String] = {
    _gridModel.removeAll()
    Success("All layers removed")
  }
  override def handleColonCommand(command: String, args: Array[String]) = command match {
    case "lock"   => lockToCells(); Success("Locked to cells")
    case "ilock"  => lockToIntersections(); Success("Locked to intersections")
    case "unlock" => unlock(); Success("Unlocked")

    case "default" => _drawStatus = ""; _drawReactions = defaultDrawReactions; publish(StatusChanged(this)); Success("Default")
    case "alpha"   => _drawStatus = "[alpha]"; _drawReactions = alphaDrawReactions; publish(StatusChanged(this)); Success("Alpha")
    case "fill"    => _drawStatus = "[fill]"; _drawReactions = fillDrawReactions; publish(StatusChanged(this)); Success("Fill")

    case "advance" => advanceEnabled = true; Success("Auto-advance enabled")
    case "noadvance" => advanceEnabled = false; Success("Auto-advance disabled")

    case "recolor" => args match {
      case Array(arg) => for (color <- PaintStringifier.parseColor(arg)) yield {
        _gridModel mapUpdateCurrent (_ match {
            case CellGriddable(RectStampContent(stamp, _), pos) => (
              CellGriddable(RectStampContent(stamp, color), pos)
            )
            case EdgeGriddable(LineStampContent(stamp, _), pos) => (
              EdgeGriddable(LineStampContent(stamp, color), pos)
            )
            case IntersectionGriddable(PointStampContent(stamp, _), pos) => (
              IntersectionGriddable(PointStampContent(stamp, color), pos)
            )
            case x => x
          })
        "Recolored current layer with " + arg
      }
      case Array(arg1, arg2) => for (
        color1 <- PaintStringifier.parseColor(arg1);
        color2 <- PaintStringifier.parseColor(arg2)) yield {
        _gridModel mapUpdateCurrent (_ match {
            case CellGriddable(RectStampContent(stamp, c), pos) if c == color1 => (
              CellGriddable(RectStampContent(stamp, color2), pos)
            )
            case EdgeGriddable(LineStampContent(stamp, c), pos) if c == color1 => (
              EdgeGriddable(LineStampContent(stamp, color2), pos)
            )
            case IntersectionGriddable(PointStampContent(stamp, c), pos) if c == color1 => (
              IntersectionGriddable(PointStampContent(stamp, color2), pos)
            )
            case x => x
          })
        "Recolored current layer " + arg1 + " with " + arg2
      }
      case _ => Failed("wrong numbers of arguments to recolor")
    }

    case "retype" => for (
      Tuple2(tspec, arg2) <- StatusUtilities.getTwoElements(args);
      c <- tspec match {
        case "cell" => for (
          rs <- StampStringifier.parseRectStamp(arg2.split(","))
        ) yield {
          _gridModel mapUpdateCurrent (_ match {
            case CellGriddable(RectStampContent(_, p), pos) =>
              CellGriddable(RectStampContent(rs, p), pos)
            case x => x
          })
          "Retyped"
        }
        case "edge" => for (
          ls <- StampStringifier.parseLineStamp(arg2.split(","))
        ) yield {
          _gridModel mapUpdateCurrent (_ match {
            case EdgeGriddable(LineStampContent(_, p), pos) =>
              EdgeGriddable(LineStampContent(ls, p), pos)
            case x => x
          })
          "Retyped"
        }
        case "intersection" => for (
          ins <- StampStringifier.parsePointStamp(arg2.split(","))
        ) yield {
          _gridModel mapUpdateCurrent (_ match {
            case IntersectionGriddable(PointStampContent(_, p), pos) =>
              IntersectionGriddable(PointStampContent(ins, p), pos)
            case x => x
          })
          "Retyped"
        }
        case _ => Failed("could not parse type " + tspec)
      }
    ) yield c

    case "newlayer" => StatusUtilities.addLayerTo(_gridModel)
    case "addlayer" => StatusUtilities.addLayerTo(_gridModel)
    case "rmlayer"  => StatusUtilities.removeLayerFrom(_gridModel)
    case "dellayer" => StatusUtilities.removeLayerFrom(_gridModel)
    case "rmall"    => removeAll()
    case "delall"   => removeAll()
    case "clear" =>
      _gridModel.clearGrid()
      Success("Content cleared")
    case "clearall" =>
      _gridModel.clearAll()
      Success("All content cleared")
    case c => Failed("unrecognized command: " + c)
  }
  def setPaintSet(ps: PaintSet[Paint]) {
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
  def setCellPaintSet(ps: PaintSet[Paint]) {
    cellPaint = ps.paint
    cellPaintName = ps.name
    createPaintStatus()
    publish(StatusChanged(this))
  }
  def setEdgePaintSet(ps: PaintSet[Paint]) {
    edgePaint = ps.paint
    edgePaintName = ps.name
    createPaintStatus()
    publish(StatusChanged(this))
  }
  def setIntersectionPaintSet(ps: PaintSet[Paint]) {
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

  def backspaceReaction: PartialFunction[List[KeyData], KeyResult] = kd => kd match {
    case List(KeyTypedData('\u0008' /*^H*/)) => {
      backspace()
      KeyComplete
    }
  }
  def repeatReaction: PartialFunction[List[KeyData], KeyResult] = kd => kd match {
    case List(KeyTypedData('q')) => {
      putStampAtSelected(lastRectStamp, lastLineStamp, lastPointStamp)
      KeyComplete
    }
  }

  def keyListReactions = (moveReactions
    orElse commandStartReactions
    orElse _drawReactions
    orElse moveAndDrawReactions
    orElse paintReactions
    orElse writeReactions
    orElse gridListReactions
    orElse backspaceReaction
    orElse repeatReaction)
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
