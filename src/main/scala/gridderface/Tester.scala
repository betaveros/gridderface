package gridderface

import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import java.io.File
import java.awt.geom.AffineTransform
import gridderface.stamp.FillRectStamp

object Tester {

  def main(args: Array[String]): Unit = {
    val img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB)
    val prov = new SimpleGridProvider(32,32)
    val cont = new RectStampContent(FillRectStamp, Color.RED)
    val griddable = new CellGriddable(cont, new CellPosition(2,2))
    griddable.grid(prov, img.createGraphics())
    
    // ImageIO.write(img, "png", new File("/Users/glacier/quick/gftest.png"))
    
  }
  def inverseSpiralSimilarity(x0: Double, y0: Double, x1: Double, y1: Double): AffineTransform = {
    val dx = x1 - x0
    val dy = y1 - y0
    val r2 = dx*dx + dy*dy
    val nx = dx / r2
    val ny = dy / r2
    new AffineTransform(nx, ny, -(x0*nx + y0*ny), -ny, nx, -(-x0*ny + y0*nx))
  }

}