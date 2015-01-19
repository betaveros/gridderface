package gridderface

import java.awt.image.BufferedImage
import scala.math.{ sqrt, abs, max }
import scala.collection.mutable.ArrayBuffer

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

  private def help(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double], ft: Double, bt: Double, fsi1: Int, bsi1: Int): Seq[Double] = {
    // we now suppose each "grid line" starts at a large value from fxs
    // and ends at a large value from bxs
    // (it could have been the other way around outside this function)
    var fsi = fsi1
    var bsi = bsi1
    var rxs = ArrayBuffer[Double]()
    val len = fxs.length max bxs.length

    while (true) {
      // find start of next line
      val nextfsi = fxs.indexWhere(_ >= ft, from = bsi + 1) match {
        case -1 => len
        case x => x
      }

      // work backwards to probable end of previous line
      val bei = bxs.lastIndexWhere(_ >= bt, end = nextfsi - 1)

      rxs :+= (fsi + bei) / 2.0

      if (nextfsi == len) return rxs.toSeq

      // find lower bound for end of next line
      val nextbsi = bxs.indexWhere(_ >= bt, from = nextfsi)
      if (nextbsi == -1) return rxs.toSeq

      fsi = nextfsi
      bsi = nextbsi
    }
    throw new AssertionError("what?")
  }
  private def getCentralIndicesWith(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double], threshold: Double): Seq[Double] = {
    val ft = fxs.max * threshold
    val bt = bxs.max * threshold
    // darn scala why do these return -1 instead of Option[Int]
    val fsi1 = fxs.indexWhere(_ >= ft)
    val bsi1 = bxs.indexWhere(_ >= bt)
    if (fsi1 == -1 || bsi1 == -1) Seq()
    else if (fsi1 < bsi1) help(fxs, bxs, ft, bt, fsi1, bsi1)
    else help(bxs, fxs, bt, ft, bsi1, fsi1)
  }
  private def regress(s: Seq[Double]): (Double, Double) = {
    var skip = s(1) - s(0)
    for (i <- 2 until s.length) {
      val s2 = (s(i) - s(0)) / i
      if (abs(s2 - skip) < 1) skip = s2
    }
    return (s(0), skip)
  }
  private def getStartStep(fxs: IndexedSeq[Double], bxs: IndexedSeq[Double]): (Double, Double) = {
    var threshold = 1.0 / 3.0

    for (i <- 1 to 20) {
      val res = getCentralIndicesWith(fxs, bxs, threshold)
      if (res.length > 1) return regress(res)
      threshold *= 0.75
    }
    return (0, fxs.length - 1) // give up
  }
  def guess(img: BufferedImage): SimpleGrid = {
    // calculate "total difference" betwen every two adjacent lines of pixels
    // always positive.
    val yForwardDiffs = for (y <- 0 until (img.getHeight - 1)) yield
      (0 until img.getWidth map (diffVs(img, _, y, 0, 1))).sum
    val yBackwardDiffs = for (y <- 1 until img.getHeight) yield
      (0 until img.getWidth map (diffVs(img, _, y, 0, -1))).sum
    val xForwardDiffs = for (x <- 0 until (img.getWidth - 1)) yield
      (0 until img.getHeight map (diffVs(img, x, _, 1, 0))).sum
    val xBackwardDiffs = for (x <- 1 until img.getWidth) yield
      (0 until img.getHeight map (diffVs(img, x, _, -1, 0))).sum

    val (y1, yd) = getStartStep(yForwardDiffs, yBackwardDiffs)
    val (x1, xd) = getStartStep(xForwardDiffs, xBackwardDiffs)
    // println("yForwardDiffs:")
    // yForwardDiffs.zipWithIndex foreach println
    // println("yBackwardDiffs:")
    // yBackwardDiffs.zipWithIndex foreach println
    // println
    // println("xForwardDiffs:")
    // xForwardDiffs.zipWithIndex foreach println
    // println("xBackwardDiffs:")
    // xBackwardDiffs.zipWithIndex foreach println
    new SimpleGrid(yd, xd, x1 + 1, y1 + 1)
  }
}
