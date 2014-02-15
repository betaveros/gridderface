package gridderface

import scala.swing.Publisher

trait GridProvider extends Publisher {
  def grid: SimpleGrid
}
