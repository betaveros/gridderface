package gridderface

import java.awt.Graphics2D
import java.awt.Image

class GriddableImageHolder(private var _image: Option[Image]) extends Griddable {
  def image = _image
  def image_=(img: Option[Image]) {_image = img; publish(GriddableChanged(this))}
  def image_=(img: Image) {_image = Option(img); publish(GriddableChanged(this))}
  
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D): Unit = {
    image foreach (x => g2d.drawImage(x, 0, 0, x.getWidth(null), x.getHeight(null), null))
  }

}
