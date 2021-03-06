package gridderface

import java.awt.Graphics2D
import scala.collection.SeqLike
import scala.collection.GenTraversable
import scala.collection.generic.CanBuildFrom

class GriddableSeq(griddables: scala.collection.immutable.Seq[Griddable])
    extends Griddable with scala.collection.immutable.Seq[Griddable] {
  // Sort of implicit mix-in for Griddable Seqs to grid themselves
  // http://stackoverflow.com/questions/7949819/scala-mixin-to-class-instance
  griddables foreach (listenTo(_))
  reactions += {
    case GriddableChanged(g) if g != this => publish(GriddableChanged(this))
  }
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D) = griddables foreach (_.drawOnGrid(grid, g2d))
  def apply(idx: Int) = griddables(idx)
  def iterator = griddables.iterator
  def length = griddables.length
  def :+(other: Griddable) = new GriddableSeq(griddables :+ other)
  def ++(other: Seq[Griddable]) = new GriddableSeq(griddables ++ other)
}
object GriddableSeq {
  val empty = new GriddableSeq(List.empty)
}
