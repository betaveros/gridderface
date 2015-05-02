package gridderface

import gridderface.stamp._
import java.awt.{ Color, Paint }
import scala.io.Source

import PaintStringifier._

object GridderfaceStringifier {
  def stringifyGriddablePositionMap(m: GriddablePositionMap): Iterable[String] = {
    m.map flatMap (_ match {
      case (CellPosition(r, c), CellGriddable(RectStampContent(s, color: Color), _)) => {
        if (s == ClearStamp) None else
          Some("c %d %d %s %s".format(r, c, stringifyColor(color), StampStringifier.stringifyRectStamp(s)))
      }
      case (EdgePosition(r, c, o), EdgeGriddable(LineStampContent(s, color: Color), _)) => {
        if (s == ClearStamp) None else {
          val ochar = o match {
            case EdgeOrientation.Horizontal => 'h'
            case EdgeOrientation.Vertical => 'v'
          }
          Some("%c %d %d %s %s".format(ochar, r, c, stringifyColor(color), StampStringifier.stringifyLineStamp(s)))
        }
      }
      case (IntersectionPosition(r, c), IntersectionGriddable(PointStampContent(s, color: Color), _)) => {
        if (s == ClearStamp) None else
          Some("i %d %d %s %s".format(r, c, stringifyColor(color), StampStringifier.stringifyPointStamp(s)))
      }
      case c => throw new IllegalArgumentException("Cannot stringify position/griddable: " + c.toString)
    })
  }
  def readGriddablePositionsFromInto(s: Source, p: ContentPutter): Status[Unit] = {
    try {
      for (line <- s.getLines()) {
        val tokens = line split "\\s+"
        val res = for (
          t0 <- StatusUtilities.getElementByIndex(tokens, 0);
          t1 <- StatusUtilities.getElementByIndex(tokens, 1);
          t2 <- StatusUtilities.getElementByIndex(tokens, 2);
          t3 <- StatusUtilities.getElementByIndex(tokens, 3);
          r <- StatusUtilities tryToInt t1;
          c <- StatusUtilities tryToInt t2;
          color <- parseColor(t3);
          ret <- tokens(0) match {
            case "c" => p.putCell(CellPosition(r, c),
              RectStampContent(StampStringifier.parseRectStamp(tokens drop 4), color)); Success(())
            case "h" => p.putEdge(EdgePosition(r, c, EdgeOrientation.Horizontal),
              LineStampContent(StampStringifier.parseLineStamp(tokens drop 4), color)); Success(())
            case "v" => p.putEdge(EdgePosition(r, c, EdgeOrientation.Vertical),
              LineStampContent(StampStringifier.parseLineStamp(tokens drop 4), color)); Success(())
            case "i" => p.putIntersection(IntersectionPosition(r, c),
              PointStampContent(StampStringifier.parsePointStamp(tokens drop 4), color)); Success(())
            case _ => Failed("Cannot parse t0: " ++ t0)
          }) yield ret
        res match {
          case Failed(s) => return Failed(s)
          case _ => // nothing
        }
      }
      Success(())
    } catch {
      case _: java.nio.charset.MalformedInputException
        => Failed("Malformed input")
    }
  }
  def parseLineContent(str: String, defaultPaint: Paint = Color.BLACK): Status[LineContent] = {
    val colonParts = str.split(":")
    val paintStat = colonParts.length match {
      case 1 => Success(defaultPaint)
      case 2 => PaintStringifier.parseColor(colonParts(1))
      case _ => Failed("Error: extra colon while parsing LineContent")
    }
    val commaParts = colonParts(0).split(",")
    val stamp = StampStringifier.parseLineStampWithStrokeDefault(colonParts(0).split(","))
    for (paint <- paintStat) yield new LineStampContent(stamp, paint)
  }
}
