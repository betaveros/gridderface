package gridderface

import java.awt._
import gridderface.stamp._

class SelectedPositionManager(initselected: Option[Position] = None) extends Griddable {
  import SelectedPositionManager._
  private var _selected: Option[Position] = initselected
  var _paint: Paint = SelectedPositionManager.greenPaint
  def paint = _paint
  private var _cellStamp: RectStamp = outlineStamp
  def cellStamp = _cellStamp
  def cellStamp_=(stamp: RectStamp) = {
    _cellStamp = stamp
    cellContent = new RectStampContent(cellStamp, paint)
    publish(GriddableChanged(this))
  }
  var cellContent = new RectStampContent(cellStamp, paint)
  var edgeContent = new LineStampContent(HexagonLineStamp(ThickStrokeVal), paint)
  var intersectionContent = new PointStampContent(CircleFixedMark(0.25, Draw(ThickStrokeVal)), paint)
  def paint_=(p: Paint): Unit = {
    _paint = p
    cellContent = new RectStampContent(cellStamp, paint)
    edgeContent = new LineStampContent(HexagonLineStamp(ThickStrokeVal), paint)
    intersectionContent = new PointStampContent(CircleFixedMark(0.25, Draw(ThickStrokeVal)), paint)
    publish(GriddableChanged(this))
  }

  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    selected match {
      case Some(p: CellPosition) => CellGriddable.drawOnGrid(cellContent, p, grid, g2d)
      case Some(p: EdgePosition) => EdgeGriddable.drawOnGrid(edgeContent, p, grid, g2d)
      case Some(p: IntersectionPosition) => IntersectionGriddable.drawOnGrid(intersectionContent, p, grid, g2d)
      case None => Unit
    }
  }
  def selected = _selected
  def selected_=(pos: Option[Position]): Unit = {
    _selected = pos
    publish(GriddableChanged(this))
  }
  def moveSelected(verticalDelta: Int, horizontalDelta: Int){
    selected = selected map
      {_.deltaPosition(verticalDelta, horizontalDelta)}
  }
}

object SelectedPositionManager {
  val outlineStamp = FullRectStamp(Draw(ThickStrokeVal))
  val downPointingStamp = DownPointingFullRectStamp(Draw(ThickStrokeVal))
  val rightPointingStamp = RightPointingFullRectStamp(Draw(ThickStrokeVal))
  val greenPaint = new GradientPaint(0f, 0f, Color.GREEN, 0.5f, 0.5f, new Color(0, 192, 0), true)
  val blueGrayPaint = new GradientPaint(0f, 0f, new Color(176, 176, 192), 0.5f, 0.5f, new Color(64, 64, 80), true)
  val redGrayPaint = new GradientPaint(0f, 0f, new Color(192, 176, 176), 0.5f, 0.5f, new Color(80, 64, 64), true)
}
