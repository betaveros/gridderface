package gridderface

import java.awt.image.BufferedImage
import scala.math.{ sqrt, abs }

object GridGuesser {
  private def rgb(n: Int) = ((n >> 16) & 0xff, (n >> 8) & 0xff, n & 0xff)
  private def posSqrt(x: Double) = if (x <= 0) 0 else sqrt(x)
  private def posDiff(c1: Int, c2: Int): Double = {
    val (r1, g1, b1) = rgb(c1)
    val (r2, g2, b2) = rgb(c2)
    posSqrt(r2 - r1) + posSqrt(g2 - g1) + posSqrt(b2 - b1)
  }
  private def diffVs(img: BufferedImage, x: Int, y: Int, dx: Int, dy: Int) =
    posDiff(img.getRGB(x, y), img.getRGB(x + dx, y + dy))

  private def getBigTwoIndices(xs: IndexedSeq[Double]) = {
    val threshold = xs.max / 3
    val ixs = xs.zipWithIndex filter (_._1 >= threshold) map (_._2)
    if (ixs.length >= 2) (ixs(0), ixs(1)) else {
      val top1 = xs.zipWithIndex.maxBy(_._1)._2
      val top2 = xs.zipWithIndex.filter(_._2 != top1).maxBy(_._1)._2
      if (top1 < top2) (top1, top2) else (top2, top1)
    }
  }
  def guess(img: BufferedImage): SimpleGrid = {
    val yForwardDiffs = for (y <- 0 until (img.getHeight - 1)) yield
      (0 until img.getWidth map (diffVs(img, _, y, 0, 1))).sum
    val yBackwardDiffs = for (y <- 1 until img.getHeight) yield
      (0 until img.getWidth map (diffVs(img, _, y, 0, -1))).sum
    val xForwardDiffs = for (x <- 0 until (img.getWidth - 1)) yield
      (0 until img.getHeight map (diffVs(img, x, _, 1, 0))).sum
    val xBackwardDiffs = for (x <- 1 until img.getWidth) yield
      (0 until img.getHeight map (diffVs(img, x, _, -1, 0))).sum

    val (yf1, yf2) = getBigTwoIndices(yForwardDiffs)
    val (yb1, yb2) = getBigTwoIndices(yBackwardDiffs)
    val (xf1, xf2) = getBigTwoIndices(xForwardDiffs)
    val (xb1, xb2) = getBigTwoIndices(xBackwardDiffs)
    // yDiffs foreach println
    // println
    // xDiffs foreach println
    new SimpleGrid(
      (xf2 - xf1 + xb2 - xb1) / 2.0,
      (yf2 - yf1 + yb2 - yb1) / 2.0,
      (xf1 + xb1) / 2.0 + 1,
      (yf1 + yb1) / 2.0 + 1)
  }
}
