package gridderface

import java.awt.{Graphics2D, Dimension, AlphaComposite}
import java.awt.image.BufferedImage

class OpacityBufferGriddable(val original: Griddable, sizefunc: () => Dimension, private var _opacity: Float) extends Griddable {
  
  listenTo(original)
  reactions += {
    case GriddableChanged(`original`) => publish(GriddableChanged(this))
  }
  def opacity = _opacity
  def opacity_=(op: Float) = {
    _opacity = op
    publish(GriddableChanged(this))
  }
  
  private var curWidth = 0
  private var curHeight = 0
  
  
  def ensureDimensions(dim: Dimension) {
    if (dim.width > curWidth) curWidth = 2*curWidth max dim.width
    if (dim.height > curHeight) curHeight = 2*curHeight max dim.height
  }
  
  def grid(prov: GridProvider, g2d: Graphics2D) {
    ensureDimensions(sizefunc())
    val buf = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_ARGB)

    val lg = buf.createGraphics()
    original.grid(prov, lg)
    
    val cs = g2d.getComposite
    g2d setComposite AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    g2d.drawImage(buf, 0, 0, buf.getWidth, buf.getHeight, null)
    g2d setComposite cs
  }

}