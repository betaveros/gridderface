package gridderface

import scala.collection.immutable.HashMap
import java.awt.Color
import gridderface.stamp._
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
  def parseLineStampString(str: String): Status[LineStamp] = StrokeVal parse str match {
    case Some(s) => Success(StrokeLineStamp(s))
    case None => Failed("Error: could not parse LineStamp " + str)
  }

  def parseLineContentString(str: String, defaultPaint: Paint = Color.BLACK): Status[LineContent] = {
    val colonParts = str.split(":")
    val paintStat = colonParts.length match {
      case 1 => Success(defaultPaint)
      case 2 => PaintStringifier.parseColor(colonParts(1))
      case _ => Failed("Error: extra colon while parsing LineContent")
    }
    for (paint <- paintStat; stamp <- parseLineStampString(colonParts(0))) yield {
      new LineStampContent(stamp, paint)
    }
  }
}
