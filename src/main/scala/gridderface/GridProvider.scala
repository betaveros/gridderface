package gridderface

import scala.swing.Publisher

trait GridProvider extends Publisher {
  def computeX(col: Int): Double
  def computeY(row: Int): Double
  def computeRow(y: Double): Int
  def computeCol(x: Double): Int
  
  def computePosition(x: Double, y: Double, edgeTolerance: Double = 3.0) = {
    val row = computeRow(y)
    val col = computeCol(x)
    
    val rowinc = if (y - computeY(row) <= edgeTolerance) 0 else
      if (computeY(row + 1) - y <= edgeTolerance) 2 else 1
    
    val colinc = if (x - computeX(col) <= edgeTolerance) 0 else
      if (computeX(col + 1) - x <= edgeTolerance) 2 else 1
    // FIXME: tightly coupled.
    Position.getPosition(2*row + rowinc, 2*col + colinc)
  }
  def immutableCopy(): GridProvider
}
