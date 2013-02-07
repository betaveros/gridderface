package gridderface

import java.awt.Paint
import java.awt.Color
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

class PaintSet(val name: String, val paint: Paint) {}

object PaintSet {
  def createColorSet(color: Color) =
    new PaintSet("#%06X".format(color.getRGB() & 0xffffff), color)
  
  val blackSet = new PaintSet("Black", Color.BLACK)
  val redSet = new PaintSet("Red", new Color(255, 0, 0))
  val greenSet = new PaintSet("Green", new Color(0, 192, 0))
  val blueSet = new PaintSet("Blue", new Color(0, 0, 255))
  val cyanSet = new PaintSet("Cyan", new Color(0, 192, 192))
  val magentaSet = new PaintSet("Magenta", new Color(192, 0, 192))
  val yellowSet = new PaintSet("Yellow", new Color(192, 192, 0))
  val graySet = new PaintSet("Gray", new Color(128, 128, 128))
  val lightGraySet = new PaintSet("LightGray", new Color(192, 192, 192))
  val lightRedSet = new PaintSet("LightRed", new Color(255, 192, 192))
  val lightGreenSet = new PaintSet("LightGreen", new Color(128, 255, 128))
  val lightBlueSet = new PaintSet("LightBlue", new Color(192, 192, 255))
  val lightCyanSet = new PaintSet("LightCyan", new Color(128, 255, 255))
  val lightMagentaSet = new PaintSet("LightMagenta", new Color(255, 128, 255))
  val lightYellowSet = new PaintSet("LightYellow", new Color(255, 255, 128))
  val whiteSet = new PaintSet("White", Color.WHITE)
  val defaultMap: HashMap[KeyData, PaintSet] = HashMap(
    KeyTypedData('a') -> blackSet,
    KeyTypedData('r') -> redSet,
    KeyTypedData('g') -> greenSet,
    KeyTypedData('b') -> blueSet,
    KeyTypedData('c') -> cyanSet,
    KeyTypedData('m') -> magentaSet,
    KeyTypedData('y') -> yellowSet,
    KeyTypedData('A') -> lightGraySet,
    KeyTypedData('R') -> lightRedSet,
    KeyTypedData('G') -> lightGreenSet,
    KeyTypedData('B') -> lightBlueSet,
    KeyTypedData('C') -> lightCyanSet,
    KeyTypedData('M') -> lightMagentaSet,
    KeyTypedData('Y') -> lightYellowSet,
    KeyTypedData('w') -> whiteSet
  )
}