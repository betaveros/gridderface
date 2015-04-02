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

  val blackSet =        new PaintSet("Black",        new Color(  0,  0,  0))
  val redSet =          new PaintSet("Red",          new Color(255,  0,  0))
  val greenSet =        new PaintSet("Green",        new Color(  0,224,  0))
  val blueSet =         new PaintSet("Blue",         new Color(  0,  0,255))
  val cyanSet =         new PaintSet("Cyan",         new Color(  0,224,224))
  val magentaSet =      new PaintSet("Magenta",      new Color(224,  0,224))
  val yellowSet =       new PaintSet("Yellow",       new Color(224,224,  0))
  val graySet =         new PaintSet("Gray",         new Color(128,128,128))

  val orangeSet =       new PaintSet("Orange",       new Color(255,128,  0))
  val indigoSet =       new PaintSet("Indigo",       new Color( 96,  0,224))
  val lemonSet =        new PaintSet("Lemon",        new Color(128,224,  0))
  val violetSet =       new PaintSet("Violet",       new Color(128,  0,255))
  val skyBlueSet =      new PaintSet("SkyBlue",      new Color(153,204,255))
  val emeraldSet =      new PaintSet("Emerald",      new Color(102,204,153))
  val fireEngineSet =   new PaintSet("FireEngine",   new Color(204, 51, 51))
  val azureSet =        new PaintSet("Azure",        new Color(  0,128,255))
  val ultramarineSet =  new PaintSet("Ultramarine",  new Color( 63,  0,255))

  val lightGraySet =    new PaintSet("LightGray",    new Color(192,192,192))
  val lightRedSet =     new PaintSet("LightRed",     new Color(255,192,192))
  val lightGreenSet =   new PaintSet("LightGreen",   new Color(128,255,128))
  val lightBlueSet =    new PaintSet("LightBlue",    new Color(192,192,255))
  val lightCyanSet =    new PaintSet("LightCyan",    new Color(128,255,255))
  val lightMagentaSet = new PaintSet("LightMagenta", new Color(255,128,255))
  val lightYellowSet =  new PaintSet("LightYellow",  new Color(255,255,128))
  val whiteSet =        new PaintSet("White",        new Color(255,255,255))
  val basicMap: HashMap[KeyData, PaintSet] = HashMap(
    KeyTypedData('r') -> redSet,
    KeyTypedData('g') -> greenSet,
    KeyTypedData('b') -> blueSet,
    KeyTypedData('c') -> cyanSet,
    KeyTypedData('m') -> magentaSet,
    KeyTypedData('y') -> yellowSet
  )
  val defaultMap: HashMap[KeyData, PaintSet] = basicMap ++ HashMap(
    KeyTypedData('k') -> blackSet,
    KeyTypedData('o') -> orangeSet,
    KeyTypedData('i') -> indigoSet,
    KeyTypedData('l') -> lemonSet,
    KeyTypedData('v') -> violetSet,
    KeyTypedData('s') -> skyBlueSet,
    KeyTypedData('e') -> emeraldSet,
    KeyTypedData('a') -> azureSet,
    KeyTypedData('u') -> ultramarineSet,
    KeyTypedData('f') -> fireEngineSet,
    KeyTypedData('K') -> lightGraySet,
    KeyTypedData('R') -> lightRedSet,
    KeyTypedData('G') -> lightGreenSet,
    KeyTypedData('B') -> lightBlueSet,
    KeyTypedData('C') -> lightCyanSet,
    KeyTypedData('M') -> lightMagentaSet,
    KeyTypedData('Y') -> lightYellowSet,
    KeyTypedData('w') -> whiteSet
  )
}
