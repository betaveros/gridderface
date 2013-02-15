package gridderface


class SimpleGridProvider(val rowHeight: Double, val colWidth: Double, val xOffset: Double = 0.0, val yOffset: Double = 0.0) extends GridProvider {

  override def computeX(col: Int): Double = xOffset + col * colWidth
  override def computeY(row: Int): Double = yOffset + row * rowHeight
  override def computeRow(y: Double): Int = ((y - yOffset) / rowHeight).toInt
  override def computeCol(x: Double): Int = ((x - xOffset) / colWidth).toInt
}