

package gridderface

import java.awt.Graphics2D
import scala.swing.Publisher

trait Griddable extends Publisher {
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit
}
