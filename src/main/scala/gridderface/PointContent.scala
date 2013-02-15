package gridderface

import java.awt.Graphics2D
import java.awt.Paint

trait PointContent {
  def draw(g2d: Graphics2D, x: Double, y: Double, r: Double): Unit
}