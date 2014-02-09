package gridderface


class SimpleGridProvider(val rowHeight: Double, val colWidth: Double, val xOffset: Double = 0.0, val yOffset: Double = 0.0) extends GridProvider {

  override def computeX(col: Int): Double = xOffset + col * colWidth
  override def computeY(row: Int): Double = yOffset + row * rowHeight
  override def computeRow(y: Double): Int = ((y - yOffset) / rowHeight).floor.toInt
  override def computeCol(x: Double): Int = ((x - xOffset) / colWidth).floor.toInt

  def offsetBy(xd: Double, yd: Double) = {
    new SimpleGridProvider(rowHeight, colWidth, xOffset + xd, yOffset + yd)
  }
  def gridSizeAdjustedBy(rd: Double, cd: Double) = {
    new SimpleGridProvider((rowHeight + rd) max 0.0, (colWidth + cd) max 0.0, xOffset, yOffset)
  }

  override def equals(other: Any) = {
    other match {
      case p: SimpleGridProvider => (
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
  override def simpleGrid = this
}

object SimpleGridProvider {
  val defaultGrid = new SimpleGridProvider(32, 32, 0, 0)
  val generationGrid = new SimpleGridProvider(32, 32, 16, 16)
}
