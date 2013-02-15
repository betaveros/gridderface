package gridderface

import java.awt.Graphics2D

trait RectContent {
	def draw(g2d: Graphics2D, x: Double, y: Double, w: Double, h: Double): Unit
}