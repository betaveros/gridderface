package gridderface

import java.awt._
import java.awt.image.BufferedImage
import scala.swing.Publisher
import java.awt.geom.AffineTransform

class OpacityBuffer(val original: Griddable,
  val gridProvider: GridProvider,
  private var _opacity: Float,
  private var _antiAlias: Boolean = false,
  private var _textAntiAlias: Boolean = false,
  private var _blendMode: OpacityBuffer.BlendMode.Value = OpacityBuffer.Normal) extends Publisher {
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
  def antiAlias = _antiAlias
  def antiAlias_=(aa: Boolean) = {
    _antiAlias = aa
    publish(BufferChanged(this))
  }
  def textAntiAlias = _textAntiAlias
  def textAntiAlias_=(aa: Boolean) = {
    _textAntiAlias = aa
    publish(BufferChanged(this))
  }
  def blendMode = _blendMode
  def blendMode_=(m: OpacityBuffer.BlendMode.Value) = {
    _blendMode = m
    publish(BufferChanged(this))
  }

  private var curWidth = 0
  private var curHeight = 0
  private var cache: Option[BufferedImage] = None
  private var cacheGrid: Option[SimpleGrid] = None
  private var cacheTransform: Option[AffineTransform] = None
  private var cacheAntiAlias: Option[Boolean] = None
  private var cacheTextAntiAlias: Option[Boolean] = None

  def ensureDimensions(dim: Dimension) {
    if (dim.width > curWidth) curWidth = (curWidth * 4 / 3) max dim.width
    else if (dim.width * 2 < curWidth) curWidth = dim.width * 4 / 3
    if (dim.height > curHeight) curHeight = (curHeight * 4 / 3) max dim.height
    else if (dim.height * 2 < curHeight) curHeight = dim.height * 4 / 3
  }

  private def recache(grid: SimpleGrid, g2d: Graphics2D, transform: AffineTransform, dim: Dimension) {
    ensureDimensions(dim)
    val buf = new BufferedImage(curWidth, curHeight, BufferedImage.TYPE_INT_ARGB)
    val lg = buf.createGraphics()
    lg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if (antiAlias) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF)
    lg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, if (textAntiAlias) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
    lg transform transform
    original.drawOnGrid(grid, lg)

    cache = Some(buf)
    cacheGrid = Some(grid)
    cacheTransform = Some(transform.clone().asInstanceOf[AffineTransform])
  }

  def fit(dd: Int, cd: Int) = dd <= cd && dd * 2 >= cd

  def draw(g2d: Graphics2D, transform: AffineTransform, dim: Dimension) {
    val grid = gridProvider.grid
    if (!(cache.nonEmpty
        && cacheGrid.exists(_ equals grid)
        && cacheTransform.exists(_ equals transform)
        && cacheAntiAlias.exists(_ equals antiAlias)
        && cacheTextAntiAlias.exists(_ equals textAntiAlias)
        && fit(dim.height, curHeight)
        && fit(dim.width, curWidth))) {
      recache(grid, g2d, transform, dim)
    }
    val buf = cache.get
    val cs = g2d.getComposite
    g2d setComposite OpacityBuffer.getComposite(blendMode, opacity)
    g2d.drawImage(buf, 0, 0, buf.getWidth, buf.getHeight, null)
    g2d setComposite cs
  }

}
object OpacityBuffer {
  object BlendMode extends Enumeration {
    val Normal, Multiply, Min = Value
  }
  val Normal   = BlendMode.Normal
  val Multiply = BlendMode.Multiply
  val Min      = BlendMode.Min
  def getComposite(m: BlendMode.Value, opacity: Float) = m match {
    case Normal   => AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    case Multiply => new MultiplyComposite(opacity)
    case Min      => new MinComposite(opacity)
  }
}
