package gridderface

sealed abstract class Position extends Ordered[Position] {
  def horizontalPosition: Int
  def verticalPosition: Int
  def deltaPosition(verticalDelta: Int, horizontalDelta: Int) =
    Position.getPosition(verticalPosition + verticalDelta,
    		horizontalPosition + horizontalDelta)
}
case class CellPosition(row: Int, col: Int) extends Position {
  def horizontalPosition = col * 2 + 1
  def verticalPosition = row * 2 + 1
  def compare(that: Position) = that match {
    case CellPosition(tr, tc) => {
      if (row != tr) row.compareTo(tr) else col.compareTo(tc)
    }
    case _ => -1
  }
    
}
case class IntersectionPosition(row: Int, col: Int) extends Position {
  def horizontalPosition = col * 2
  def verticalPosition = row * 2
  def compare(that: Position) = that match {
    case IntersectionPosition(tr, tc) => {
      if (row != tr) row.compare(tr) else col.compare(tc)
    }
    case _ => 1
  }
}
object EdgeOrientation extends Enumeration {
  type EdgeOrientation = Value
  val Horizontal = Value("Horizontal")
  val Vertical = Value("Vertical")
}
import EdgeOrientation._
case class EdgePosition(row: Int, col: Int, orientation: EdgeOrientation) extends Position {
  def horizontalPosition = orientation match {
    case Horizontal => col * 2 + 1
    case Vertical => col * 2
  }
  def verticalPosition = orientation match {
    case Horizontal => row * 2
    case Vertical => row * 2 + 1
  }
  def compare(that: Position) = that match {
    case EdgePosition(tr, tc, torient) => {
      if (row != tr) row.compare(tr) else
        if (col != tc) col.compare(tc) else {
          if (orientation != torient) {
            if (orientation == Horizontal) -1 else 1
          } else 0
        }
    }
    case CellPosition(_, _) => 1
    case IntersectionPosition(_, _) => -1
  }
}

object Position {
  private def flooredHalf(a: Int) = {
    if (a >= 0) a / 2 else (a-1) / 2
  }
  def getPosition(vertical: Int, horizontal: Int): Position = {
    val col = flooredHalf(horizontal)
    val row = flooredHalf(vertical)
    if (horizontal % 2 == 0) {
      if (vertical % 2 == 0) new IntersectionPosition(row, col)
      else new EdgePosition(row, col, Vertical)
    } else {
      if (vertical % 2 == 0) new EdgePosition(row, col, Horizontal) 
      else new CellPosition(row, col)
    }
  }
  
}
