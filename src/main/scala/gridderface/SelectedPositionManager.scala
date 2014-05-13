package gridderface

import java.awt.Graphics2D
import java.awt.Color
import gridderface.stamp._
import java.awt.GradientPaint

class SelectedPositionManager(initselected: Option[Position] = None) extends Griddable {
  private var _selected: Option[Position] = initselected
  val paint = new GradientPaint(0f, 0f, Color.GREEN, 0.5f, 0.5f, new Color(0, 192, 0), true)
  val cellContent = new RectStampContent(new OutlineRectStamp(Strokes.thickStroke), paint)
  val edgeContent = new LineStampContent(new HexagonLineStamp(Strokes.thickStroke), paint)
  val intersectionContent = new PointStampContent(FixedMark.createCircleStamp(0.25, Strokes.thickStroke), paint)

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
