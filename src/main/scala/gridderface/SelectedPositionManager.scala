package gridderface

import java.awt._
import gridderface.stamp._

class SelectedPositionManager(initselected: Option[Position] = None) extends Griddable {
  private var _selected: Option[Position] = initselected
  var _paint: Paint = SelectedPositionManager.greenPaint
  def paint = _paint
  var cellContent = new RectStampContent(OutlineRectStamp(ThickStrokeVal), paint)
  var edgeContent = new LineStampContent(HexagonLineStamp(ThickStrokeVal), paint)
  var intersectionContent = new PointStampContent(CircleFixedMark(0.25, ThickStrokeVal), paint)
  def paint_=(p: Paint): Unit = {
    _paint = p
    cellContent = new RectStampContent(OutlineRectStamp(ThickStrokeVal), paint)
    edgeContent = new LineStampContent(HexagonLineStamp(ThickStrokeVal), paint)
    intersectionContent = new PointStampContent(CircleFixedMark(0.25, ThickStrokeVal), paint)
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
  val greenPaint = new GradientPaint(0f, 0f, Color.GREEN, 0.5f, 0.5f, new Color(0, 192, 0), true)
  val blueGrayPaint = new GradientPaint(0f, 0f, new Color(176, 176, 192), 0.5f, 0.5f, new Color(64, 64, 80), true)
  val redGrayPaint = new GradientPaint(0f, 0f, new Color(192, 176, 176), 0.5f, 0.5f, new Color(80, 64, 64), true)
}
