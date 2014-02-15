package gridderface


class SimpleGrid(val rowHeight: Double, val colWidth: Double, val xOffset: Double = 0.0, val yOffset: Double = 0.0) {

  def computeX(col: Int): Double = xOffset + col * colWidth
  def computeY(row: Int): Double = yOffset + row * rowHeight
  def computeRow(y: Double): Int = ((y - yOffset) / rowHeight).floor.toInt
  def computeCol(x: Double): Int = ((x - xOffset) / colWidth).floor.toInt

  def computePosition(x: Double, y: Double, edgeTolerance: Double = 3.0) = {
    val row = computeRow(y)
    val col = computeCol(x)

    val rowinc = if (y - computeY(row) <= edgeTolerance) 0 else
      if (computeY(row + 1) - y <= edgeTolerance) 2 else 1
    val colinc = if (x - computeX(col) <= edgeTolerance) 0 else
      if (computeX(col + 1) - x <= edgeTolerance) 2 else 1
    // tightly coupled?
    Position.getPosition(2*row + rowinc, 2*col + colinc)
  }
  def isXCoordinateOnEdge(x: Double, tol: Double = 3.0) = {
    val col = computeCol(x)
    (x - computeX(col)) <= tol || (computeX(col + 1) - x) <= tol
  }
  def isYCoordinateOnEdge(y: Double, tol: Double = 3.0) = {
    val row = computeRow(y)
    (y - computeY(row)) <= tol || (computeY(row + 1) - y) <= tol
  }

  def offsetBy(xd: Double, yd: Double) = {
    new SimpleGrid(rowHeight, colWidth, xOffset + xd, yOffset + yd)
  }
  def gridSizeAdjustedBy(rd: Double, cd: Double) = {
    new SimpleGrid((rowHeight + rd) max 0.0, (colWidth + cd) max 0.0, xOffset, yOffset)
  }

  override def equals(other: Any) = {
    other match {
      case p: SimpleGrid => (
        rowHeight == p.rowHeight &&
        colWidth == p.colWidth &&
        xOffset == p.xOffset &&
        yOffset == p.yOffset)
      case _ => false
    }
  }
  private lazy val _hash = {
    rowHeight.##() + 13*colWidth.##() + 37*xOffset.##() + 79*yOffset.##()
  }
  override def hashCode() = _hash
}

object SimpleGrid {
  val defaultGrid = new SimpleGrid(32, 32, 0, 0)
  val generationGrid = new SimpleGrid(32, 32, 16, 16)
}
