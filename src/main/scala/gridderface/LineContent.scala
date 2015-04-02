package gridderface

import java.awt.Graphics2D
import java.awt.Paint

trait LineContent {
  def draw(g2d: Graphics2D, x1: Double, y1: Double, x2: Double, y2: Double, transverseDimension: Double): Unit
}
