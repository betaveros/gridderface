package gridderface

import java.awt.{Graphics2D, Dimension, AlphaComposite}
import java.awt.image.BufferedImage
import scala.swing.Publisher
import java.awt.geom.AffineTransform

class OpacityBuffer(val original: Griddable, private var _opacity: Float) extends Publisher {
  // okay, so one reasonably obvious way to speed up Gridderface is to cache the
  // stuff that this buffer does. Unfortunately, my crappy and arguably
  // severely overgeneralized current method of implementation requires that the
  // buffer know the grid and transform it's dealing with before drawing
  // anything. The reason is since it wants to create a uniformly translucent
  // image, it needs to know the dimensions of an image to draw on that will
  // get shown on the outside.

  // Right now, I'm just caching both the transform and grid to check if my
  // cached image is valid, since they probably won't change much.

  // TODO: In the future, maybe invert the transform and allocate a buffer image
  // before it, so we don't have to cache on it too?

  
  listenTo(original)
  reactions += {
    case GriddableChanged(`original`) => {
      cache = None
      publish(BufferChanged(this))
    }
  }
  def opacity = _opacity
  def opacity_=(op: Float) = {
    _opacity = op
    publish(BufferChanged(this))
  }
  
  private var curWidth = 0
  private var curHeight = 0
  private var cache: Option[BufferedImage] = None
  private var cacheGrid: Option[SimpleGrid] = None
  private var cacheTransform: Option[AffineTransform] = None
  
  def ensureDimensions(dim: Dimension) {
    if (dim.width > curWidth) curWidth = 2*curWidth max dim.width
    if (dim.height > curHeight) curHeight = 2*curHeight max dim.height
  }

  private def recache(grid: SimpleGrid, g2d: Graphics2D, transform: AffineTransform, dim: Dimension) {
    ensureDimensions(dim)
    val buf = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_ARGB)
    val lg = buf.createGraphics()
    lg transform transform
    original.drawOnGrid(grid, lg)

    cache = Some(buf)
    cacheGrid = Some(grid)
    cacheTransform = Some(transform.clone().asInstanceOf[AffineTransform])
  }
  
  def drawOnGrid(grid: SimpleGrid, g2d: Graphics2D, transform: AffineTransform, dim: Dimension) {
    if (!(cache.nonEmpty
        && cacheGrid.exists(_ equals grid)
        && cacheTransform.exists(_ equals transform)
        && dim.height <= curHeight
        && dim.width <= curWidth)) {
      recache(grid, g2d, transform, dim)
    }
    val buf = cache.get
    val cs = g2d.getComposite
    g2d setComposite AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    g2d.drawImage(buf, 0, 0, buf.getWidth, buf.getHeight, null)
    g2d setComposite cs
  }

}
