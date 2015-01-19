package gridderface

import gridderface.stamp._
import java.awt.Color
import scala.io.Source

object GridderfaceStringifier {
  def stringifyColor(c: Color): String = Seq(c.getRed(), c.getGreen(), c.getBlue()).mkString(",")
  def parseColor(s: String): Status[Color] = s.split(",") match {
    case Array(rs, gs, bs) => for (
      r <- CommandUtilities tryToInt rs;
      g <- CommandUtilities tryToInt gs;
      b <- CommandUtilities tryToInt bs
    ) yield new Color(r, g, b)
    case _ => Failed("Color does not have 3 components")
  }
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
    })
  }
  def readGriddablePositionsFromInto(s: Source, p: ContentPutter): Status[Unit] = {
    try {
      for (line <- s.getLines()) {
        val tokens = line split "\\s+"
        val res = for (
          t0 <- CommandUtilities.getElementByIndex(tokens, 0);
          t1 <- CommandUtilities.getElementByIndex(tokens, 1);
          t2 <- CommandUtilities.getElementByIndex(tokens, 2);
          t3 <- CommandUtilities.getElementByIndex(tokens, 3);
          r <- CommandUtilities tryToInt t1;
          c <- CommandUtilities tryToInt t2;
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
}
