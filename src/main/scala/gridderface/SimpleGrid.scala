package gridderface


case class SimpleGrid(rowHeight: Double, colWidth: Double, xOffset: Double = 0.0, yOffset: Double = 0.0) {

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
    SimpleGrid(rowHeight, colWidth, xOffset + xd, yOffset + yd)
  }
  def gridSizeAdjustedBy(rd: Double, cd: Double) = {
    SimpleGrid((rowHeight + rd) max 0.0, (colWidth + cd) max 0.0, xOffset, yOffset)
  }
}

object SimpleGrid {
  val defaultGrid    = SimpleGrid(32, 32, 0, 0)
  val generationGrid = SimpleGrid(32, 32, 16, 16)
  def stringify(g: SimpleGrid): String = g match {
    case SimpleGrid(rh, cw, xoff, yoff) => "%s %s %s %s".format(rh.toString, cw.toString, xoff.toString, yoff.toString)
  }
  def parse(s: String): Option[SimpleGrid] = {
    val parts = "\\s+".r.split(s.substring(2))
    try {
      parts match {
        case Array(s1, s2, s3, s4) =>
          Some(SimpleGrid(s1.toDouble, s2.toDouble, s3.toDouble, s4.toDouble))
        case _ => None
      }
    } catch {
      case _: NumberFormatException => None
    }
  }
}
