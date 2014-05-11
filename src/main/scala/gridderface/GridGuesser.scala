package gridderface

import java.awt.image.BufferedImage
import scala.math.{ sqrt, abs, max }

object GridGuesser {
  private def rgb(n: Int) = ((n >> 16) & 0xff, (n >> 8) & 0xff, n & 0xff)
  private def damp(x: Double) = if (x <= 0) 0 else sqrt(sqrt(x))
  private def posDiff(c1: Int, c2: Int): Double = {
    val (r1, g1, b1) = rgb(c1)
    val (r2, g2, b2) = rgb(c2)
    damp(r2 - r1) + damp(g2 - g1) + damp(b2 - b1)
  }
  private def diffVs(img: BufferedImage, x: Int, y: Int, dx: Int, dy: Int) =
    posDiff(img.getRGB(x, y), img.getRGB(x + dx, y + dy))

  private def help(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double], ft: Double, bt: Double, fsi1: Int, bsi1: Int): Option[(Double, Double)] = {
    val len = fxs.length max bxs.length

    // find start of second line
    val fsi2 = fxs.indexWhere(_ >= ft, from = bsi1 + 1)
    if (fsi2 == -1) return None

    // work backwards to probable end of first line
    val bei1 = bxs.lastIndexWhere(_ >= bt, end = fsi2 - 1)

    // find lower bound for end of second line
    val bsi2 = bxs.indexWhere(_ >= bt, from = fsi2)
    if (bsi2 == -1) return None

    // find start of third line, or just ending boundary
    val fsi3 = fxs.indexWhere(_ >= ft, from = bsi2 + 1) match { case -1 => len; case x => x }

    // work backwards to probable end of second line
    val bei2 = bxs.lastIndexWhere(_ >= bt, end = fsi3 - 1)

    return Some((fsi1 + bei1) / 2.0, (fsi2 + bei2) / 2.0)
  }
  private def getTwoCentralIndicesWith(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double], threshold: Double): Option[(Double, Double)] = {
    val ft = fxs.max * threshold
    val bt = bxs.max * threshold
    // darn scala why do these return -1 instead of Option[Int]
    val fsi1 = fxs.indexWhere(_ >= ft)
    val bsi1 = bxs.indexWhere(_ >= bt)
    if (fsi1 == -1 || bsi1 == -1) None
    else if (fsi1 < bsi1) help(fxs, bxs, ft, bt, fsi1, bsi1)
    else help(bxs, fxs, bt, ft, bsi1, fsi1)
  }

  private def getTwoIndices(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double]): (Double, Double) = {
    var threshold = 1.0 / 3.0

    for (i <- 1 to 20) {
      getTwoCentralIndicesWith(fxs, bxs, threshold) match {
        case Some(res) => return res
        case None => threshold *= 0.75
      }
    }
    return (0, fxs.length - 1) // give up
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

    val (y1, y2) = getTwoIndices(yForwardDiffs, yBackwardDiffs)
    val (x1, x2) = getTwoIndices(xForwardDiffs, xBackwardDiffs)
    // println("yForwardDiffs:")
    // yForwardDiffs.zipWithIndex foreach println
    // println("yBackwardDiffs:")
    // yBackwardDiffs.zipWithIndex foreach println
    // println
    // println("xForwardDiffs:")
    // xForwardDiffs.zipWithIndex foreach println
    // println("xBackwardDiffs:")
    // xBackwardDiffs.zipWithIndex foreach println
    new SimpleGrid(y2 - y1, x2 - x1, x1 + 1, y1 + 1)
  }
}
