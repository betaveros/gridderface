package gridderface

import java.awt.{ Color, Paint }
import scala.collection.immutable.HashMap

object PaintStringifier {
  def optionHexToInt(c: Char) = {
    if ('0' <= c && c <= '9') Some(c - '0')
    else if ('a' <= c && c <= 'f') Some(c - 'a' + 10)
    else if ('A' <= c && c <= 'F') Some(c - 'A' + 10)
    else None
  }
  def optionHexStringToInt(str: String) = {
    (str map optionHexToInt).foldLeft(Some(0): Option[Int])((acc, nextDig) =>
      for (a <- acc; n <- nextDig) yield 16 * a + n)
  }
  def newGrayColor(int: Int) = {
    new Color(int, int, int)
  }
  def optionHexStringToColor(str: String) = {
    str.length match {
      case 1 => optionHexToInt(str(0)) map { hdig => newGrayColor(hdig * 17) }
      case 3 => for (
        rdig <- optionHexToInt(str(0));
        gdig <- optionHexToInt(str(1));
        bdig <- optionHexToInt(str(2))
      ) yield new Color(rdig * 17, gdig * 17, bdig * 17)
      case 6 => for (
        rbyte <- optionHexStringToInt(str.substring(0, 2));
        gbyte <- optionHexStringToInt(str.substring(2, 4));
        bbyte <- optionHexStringToInt(str.substring(4, 6))
      ) yield new Color(rbyte, gbyte, bbyte)
      case _ => None
    }
  }
  def stringifyColor(c: Color): String = Seq(c.getRed(), c.getGreen(), c.getBlue()).mkString(",")
  val namedPaintMap = HashMap(
    "red"     -> PaintSet.redSet,
    "blue"    -> PaintSet.blueSet,
    "green"   -> PaintSet.greenSet,
    "cyan"    -> PaintSet.cyanSet,
    "magenta" -> PaintSet.magentaSet,
    "yellow"  -> PaintSet.yellowSet,
    "devred"  -> PaintSet.devRedSet,
    "black"   -> PaintSet.blackSet,
    "gray"    -> PaintSet.graySet,
    "white"   -> PaintSet.whiteSet)
  def parseColor(s: String): Status[Color] = {
    if (s.length == 0) return Failed("empty color string")
    if (s(0) == '#') {
      optionHexStringToColor(s.substring(1)) match {
        case Some(c) => Success(c)
        case None => Failed("could not parse hex color: " + s)
      }
    } else if (s(0).isDigit) {
      s.split(",") match {
        case Array(rs, gs, bs) => for (
          r <- StatusUtilities tryToInt rs;
          g <- StatusUtilities tryToInt gs;
          b <- StatusUtilities tryToInt bs
        ) yield new Color(r, g, b)
        case _ => Failed("color does not have 3 components")
      }
    } else {
      namedPaintMap get s match {
        case Some(set) => Success(set.paint)
        case None => Failed("cannot parse paint set " ++ s)
      }
    }
  }
  def parsePaintSet(s: String): Status[PaintSet[Paint]] = {
    namedPaintMap get s match {
      case Some(set) => Success(set)
      case None => parseColor(s) map (PaintSet.createColorSet(_))
    }
  }
}
