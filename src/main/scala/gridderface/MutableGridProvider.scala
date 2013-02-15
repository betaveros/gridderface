package gridderface


class MutableGridProvider(private var _rowHeight: Double,
    private var _colWidth: Double, 
    private var _xOffset: Double = 0.0,
    private var _yOffset: Double = 0.0) extends GridProvider {

  def rowHeight = _rowHeight
  def rowHeight_=(rh: Double) {_rowHeight = rh; publish(GridChanged())}
  def colWidth = _colWidth
  def colWidth_=(cw: Double) {_colWidth = cw; publish(GridChanged())}
  def xOffset = _xOffset
  def xOffset_=(xo: Double) {_xOffset = xo; publish(GridChanged())}
  def yOffset = _yOffset
  def yOffset_=(yo: Double) {_yOffset = yo; publish(GridChanged())}
  
  override def computeX(col: Int): Double = xOffset + col * colWidth
  override def computeY(row: Int): Double = yOffset + row * rowHeight
  override def computeRow(y: Double): Int = ((y - yOffset) / rowHeight).floor.toInt
  override def computeCol(x: Double): Int = ((x - xOffset) / colWidth).floor.toInt
  
  def isXCoordinateOnEdge(x: Double, tol: Double = 3.0) = {
    val col = computeCol(x)
    (x - computeX(col)) <= tol || (computeX(col + 1) - x) <= tol
  }
  def isYCoordinateOnEdge(y: Double, tol: Double = 3.0) = {
    val row = computeRow(y)
    (y - computeY(row)) <= tol || (computeY(row + 1) - y) <= tol
  }
  
}