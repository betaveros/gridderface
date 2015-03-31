package gridderface.stamp

import java.awt._
import java.awt.geom._

case class SudokuPencilRectStamp(strings: Seq[Seq[String]]) extends TextRectStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    prepare(g2d)
    g2d.setFont(SudokuPencilRectStamp.normalFont)
    val yu = TextRectStamp.magicSize / strings.length / 2
    for ((row, i) <- strings.zipWithIndex) {
      val xu = TextRectStamp.magicSize / row.length / 2
      for ((s, j) <- row.zipWithIndex) {
        drawCentered(g2d, xu * (2*j + 1), yu * (2*i + 1), s)
      }
    }
  }
}
object SudokuPencilRectStamp {
  val normalFont = TextRectStamp.font10
  def createSudokuPencilRectStamp(cands: Seq[Boolean]): SudokuPencilRectStamp = {
    if (cands.length != 9) {
      throw new IllegalArgumentException("Not nine candidates")
    }
    new SudokuPencilRectStamp(
      for (i <- 0 to 6 by 3) yield
      for (j <- 0 to 2) yield
      if (cands(i + j)) (i + j + 1).toString else ""
    )
  }
  def createSudokuPencilRectStampFromString(s: String): SudokuPencilRectStamp = {
    new SudokuPencilRectStamp(
      for (i <- 0 to 6 by 3) yield
      for (j <- 0 to 2) yield {
        val c = ('1' + i + j).toChar
        if (s contains c) c.toString else ""
      }
    )
  }
}
