package gridderface

import scala.collection.immutable.HashMap
import java.awt.Color
import gridderface.stamp.LineStamp
import gridderface.stamp.RectStamp
import gridderface.stamp.Strokes
import gridderface.stamp.OneTextRectStamp
import java.awt.Paint

object GridderfaceStringParser {
  def optionToInt(str: String): Option[Int] = {
    try {
      Some(str.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }
  def stripOptionToInt(prefix: String, str: String): Option[Int] = {
    if (str.startsWith(prefix)) {
      optionToInt(str substring prefix.length)
    } else None
  }
  def parseRectStampString(str: String): Status[RectStamp] = {
    ((optionToInt(str) map (int => new OneTextRectStamp(int.toString))) orElse
      (stripOptionToInt("o", str) map (int => new OneTextRectStamp(int.toString))) orElse None) match {
      case Some(s) => Success(s)
      case None => Failed("Error: could not parse RectStamp " + str)
    }
  }
  val lineStampMap = HashMap(
    "n" -> Strokes.normalStamp,
    "t" -> Strokes.thickStamp,
    "s" -> Strokes.thinStamp,
    "d" -> Strokes.thinDashedStamp,
    "nd" -> Strokes.normalDashedStamp)
  def parseLineStampString(str: String): Status[LineStamp] = lineStampMap get str match {
    case Some(s) => Success(s)
    case None => Failed("Error: could not parse LineStamp " + str)
  }

  val namedPaintMap = HashMap(
    "red" -> PaintSet.redSet,
    "blue" -> PaintSet.blueSet,
    "black" -> PaintSet.blackSet,
    "gray" -> PaintSet.graySet,
    "white" -> PaintSet.whiteSet)
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
  def parseColorString(str: String): Status[PaintSet] = {
    if (str.length > 0 && str(0) == '#') {
      optionHexStringToColor(str.substring(1)) map (PaintSet.createColorSet(_)) match {
        case Some(set) => Success(set)
        case None => Failed("Error: could not parse hex color: " + str)
      }
    } else {
      namedPaintMap get str match {
        case Some(set) => Success(set)
        case None => Failed("Error: undefined color name: " + str)
      }
    }
  }
  def parseLineContentString(str: String, defaultPaint: Paint = Color.BLACK): Status[LineContent] = {
    val colonParts = str.split(":")
    val paintStat = colonParts.length match {
      case 1 => Success(defaultPaint)
      case 2 => parseColorString(colonParts(1)) map (_.paint)
      case _ => Failed("Error: extra colon while parsing LineContent")
    }
    for (paint <- paintStat; stamp <- parseLineStampString(colonParts(0))) yield {
      new LineStampContent(stamp, paint)
    }
  }
}
