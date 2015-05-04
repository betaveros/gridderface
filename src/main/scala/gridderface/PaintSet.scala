package gridderface

import java.awt.Paint
import java.awt.Color
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

case class PaintSet[+A <: Paint](name: String, paint: A) {
  def darkerSet(implicit ev: A => Color) = PaintSet("Dark " ++ name, new Color(
      paint.getRed / 2,
      paint.getGreen / 2,
      paint.getBlue / 2))
  def lighterSet(implicit ev: A => Color) = PaintSet("Light " ++ name, new Color(
      (paint.getRed + 255) / 2,
      (paint.getGreen + 255) / 2,
      (paint.getBlue + 255) / 2))
}

object PaintSet {
  def createColorSet(color: Color) =
    new PaintSet("#%06X".format(color.getRGB() & 0xffffff), color)

  val blackSet        = PaintSet("Black",        new Color(  0,  0,  0))
  val redSet          = PaintSet("Red",          new Color(255,  0,  0))
  val greenSet        = PaintSet("Green",        new Color(  0,224,  0))
  val blueSet         = PaintSet("Blue",         new Color(  0,  0,255))
  val cyanSet         = PaintSet("Cyan",         new Color(  0,224,224))
  val magentaSet      = PaintSet("Magenta",      new Color(224,  0,224))
  val yellowSet       = PaintSet("Yellow",       new Color(224,224,  0))
  val graySet         = PaintSet("Gray",         new Color(128,128,128))
  val whiteSet        = PaintSet("White",        new Color(255,255,255))

  val lightGraySet    = PaintSet("LightGray",    new Color(192,192,192))
  val lightRedSet     = PaintSet("LightRed",     new Color(255,192,192))
  val lightGreenSet   = PaintSet("LightGreen",   new Color(128,255,128))
  val lightBlueSet    = PaintSet("LightBlue",    new Color(192,192,255))
  val lightCyanSet    = PaintSet("LightCyan",    new Color(128,255,255))
  val lightMagentaSet = PaintSet("LightMagenta", new Color(255,128,255))
  val lightYellowSet  = PaintSet("LightYellow",  new Color(255,255,128))

  val azureSet        = PaintSet("Azure",        new Color(  0,128,255))
  val emeraldSet      = PaintSet("Emerald",      new Color(102,204,153))
  val fireEngineSet   = PaintSet("FireEngine",   new Color(204, 51, 51))
  val indigoSet       = PaintSet("Indigo",       new Color( 96,  0,224))
  val lemonSet        = PaintSet("Lemon",        new Color(128,224,  0))
  val orangeSet       = PaintSet("Orange",       new Color(255,128,  0))
  val skyBlueSet      = PaintSet("SkyBlue",      new Color(153,204,255))
  val ultramarineSet  = PaintSet("Ultramarine",  new Color( 63,  0,255))
  val violetSet       = PaintSet("Violet",       new Color(128,  0,255))

  val amberSet        = PaintSet("Amber",        new Color(255,204,  0))
  val cardinalSet     = PaintSet("Cardinal",     new Color(204,  0, 51))

  val devRedSet       = PaintSet("DevRed",       new Color(204,  0,  0))
  // d3.js ordinal categorical colors
  val d3Map: Map[KeyData, PaintSet[Color]] = List(
    new Color(31,119,180),
    new Color(255,127,14),
    new Color(44,160,44),
    new Color(214,39,40),
    new Color(148,103,189),
    new Color(140,86,75),
    new Color(227,119,194),
    new Color(127,127,127),
    new Color(188,189,34),
    new Color(23,190,207)
  )
    .zipWithIndex
    .map({case (c, i) => (
      KeyTypedData(('0' + (i + 1) % 10).toChar)
      -> PaintSet("d3/" ++ (i + 1).toString, c))})
    .toMap

  val basicMap: HashMap[KeyData, PaintSet[Color]] = HashMap(
    KeyTypedData('r') -> redSet,
    KeyTypedData('g') -> greenSet,
    KeyTypedData('b') -> blueSet,
    KeyTypedData('c') -> cyanSet,
    KeyTypedData('m') -> magentaSet,
    KeyTypedData('y') -> yellowSet
  )
  val defaultMap: Map[KeyData, PaintSet[Color]] = basicMap ++ d3Map ++ HashMap(
    KeyTypedData('K') -> lightGraySet,
    KeyTypedData('R') -> lightRedSet,
    KeyTypedData('G') -> lightGreenSet,
    KeyTypedData('B') -> lightBlueSet,
    KeyTypedData('C') -> lightCyanSet,
    KeyTypedData('M') -> lightMagentaSet,
    KeyTypedData('Y') -> lightYellowSet,
    KeyTypedData('k') -> blackSet,
    KeyTypedData('w') -> whiteSet,

    // unused: d,h,j,n,p,q,t,x,z
    KeyTypedData('a') -> azureSet,
    KeyTypedData('e') -> emeraldSet,
    KeyTypedData('f') -> fireEngineSet,
    KeyTypedData('i') -> indigoSet,
    KeyTypedData('l') -> lemonSet,
    KeyTypedData('o') -> orangeSet,
    KeyTypedData('s') -> skyBlueSet,
    KeyTypedData('u') -> ultramarineSet,
    KeyTypedData('v') -> violetSet,

    KeyTypedData('&') -> amberSet,
    KeyTypedData('^') -> cardinalSet,
    KeyTypedData('$') -> devRedSet
  )
}
