package gridderface.stamp

import gridderface._
import gridderface.StatusUtilities._

// I don't have good terminology for this stuff
// I would like not to use "toString" or "serialize" since there are
// methods/interfaces already using them
object StampStringifier {
  def quoteString(s: String): String = {
    // I'm lazy. Just allow me to split by spaces.
    s.map(_ match {
        case ' ' => "_"
        case '\n' => "\\n"
        case '\t' => "\\t"
        case '\\' => "\\\\"
        case '_' => "\\_"
        case c => c.toString
      }).mkString("")
  }
  def unquoteString(s: String): String = {
    val b = new StringBuilder
    var i = 0
    while (i < s.length) {
      s(i) match {
        case '_' => b += ' '
        case '\\' => {
          s(i+1) match {
            case 'n' => b += '\n'
            case 't' => b += '\t'
            case '\\' => b += '\\'
            case '_' => b += '_'
            case c => { b += '\\'; b += c }
          }
          i += 1
        }
        case c => b += c
      }
      i += 1
    }
    b.toString
  }

  private def sb(b: Boolean): String = if (b) "1" else "0"
  private def bs(s: String) = s match {
    case "0" => false
    case "1" => true
    case _ => throw new IllegalArgumentException("cannot parse boolean: " ++ s)
  }

  def parseDrawVal(s: String) = DrawVal parse s match {
    case None => Failed("cannot parse DrawVal " ++ s)
    case Some(v) => Success(v)
  }
  def parseStrokeVal(s: String) = StrokeVal parse s match {
    case None => Failed("cannot parse StrokeVal " ++ s)
    case Some(v) => Success(v)
  }
  def parseArrowTextArrow(s: String) = ArrowTextArrow parse s match {
    case None => Failed("cannot parse ArrowTextArrow " ++ s)
    case Some(a) => Success(a)
  }
  def parseFontSize(s: String) = OneTextRectStamp parse s match {
    case None => Failed("cannot parse OneTextRectStamp.FontSize " ++ s)
    case Some(z) => Success(z)
  }
  def parseInequality(s: String) = InequalityLineStamp parse s match {
    case None => Failed("cannot parse InequalifyLineStamp.Inequality " ++ s)
    case Some(i) => Success(i)
  }

  def stringifyRectStamp(s: RectStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case BigCrossStamp => "x"
    case SmallCrossStamp => "smx"
    case MajorDiagonalStamp => "mjdg"
    case MinorDiagonalStamp => "mndg"
    case BigCheckStamp => "bigv"
    case SmallCheckStamp => "smv"
    case FullRectStamp(dv) => "f %s".format(DrawVal.stringify(dv))
    case CircleRectStamp(size, dv, xoff, yoff) => "o %s %s %s %s".format(size.toString, DrawVal.stringify(dv), xoff.toString, yoff.toString)
    case RectangleArcRectStamp(size, dv, tr, br, bl, tl, xoff, yoff) => "ra %s %s %s %s %s %s %s %s".format(size.toString, DrawVal.stringify(dv), sb(tr), sb(br), sb(bl), sb(tl), xoff.toString, yoff.toString)
    case DiagonalFillRectStamp => "dgf"
    case DashedFillRectStamp => "dsf"
    case DottedFillRectStamp => "dtf"
    case HorizontalLineStamp => "hl"
    case VerticalLineStamp => "vl"
    case PlusStamp => "plus"
    case StarStamp => "star"
    case ArrowStamp(dx, dy) => "arr %s %s".format(dx, dy)
    case ArrowTextRectStamp(s, arr, sv) => "tarr %s %s %s".format(quoteString(s), ArrowTextArrow.stringify(arr), StrokeVal.stringify(sv))
    case OneTextRectStamp(s1, fsv, hAlign, vAlign) => "t %s %s %s %s".format(quoteString(s1), OneTextRectStamp.stringify(fsv), hAlign.toString, vAlign.toString)
    case TwoTextRectStamp(s1, s2) => "t2 %s %s".format(quoteString(s1), quoteString(s2))
    case ThreeTextRectStamp(s1, s2, s3) => "t3 %s %s %s".format(quoteString(s1), quoteString(s2), quoteString(s3))
    case FourTextRectStamp(s1, s2, s3, s4) => "t4 %s %s %s %s".format(quoteString(s1), quoteString(s2), quoteString(s3), quoteString(s4))
    case _ => throw new IllegalArgumentException("RectStamp cannot be stringified: " ++ s.toString)
  }
  def parseRectStamp(tokens: Seq[String]): Status[RectStamp] = tokens(0) match {
    case "clear" => Success(        ClearStamp)
    case "x"     => Success(     BigCrossStamp)
    case "smx"   => Success(   SmallCrossStamp)
    case "mjdg"  => Success(MajorDiagonalStamp)
    case "mndg"  => Success(MinorDiagonalStamp)
    case "bigv"  => Success(     BigCheckStamp)
    case "smv"   => Success(   SmallCheckStamp)
    case "f" => for (dv <- parseDrawVal(tokens(1))) yield FullRectStamp(dv)
    case "o" => for (
      sz <- tryToDouble (tokens(1));
      dv <- parseDrawVal(tokens(2));
      xo <- tryToDouble (tokens(3));
      yo <- tryToDouble (tokens(4))) yield CircleRectStamp(sz, dv, xo, yo)
    case "ra" => for (
      sz <- tryToDouble (tokens(1));
      dv <- parseDrawVal(tokens(2));
      b1 <- tryToBoolean(tokens(3));
      b2 <- tryToBoolean(tokens(4));
      b3 <- tryToBoolean(tokens(5));
      b4 <- tryToBoolean(tokens(6));
      xo <- tryToDouble (tokens(7));
      yo <- tryToDouble (tokens(8))) yield RectangleArcRectStamp(sz, dv, b1, b2, b3, b4, xo, yo)
    case "dgf"  => Success(DiagonalFillRectStamp)
    case "dsf"  => Success(  DashedFillRectStamp)
    case "dtf"  => Success(  DottedFillRectStamp)
    case "hl"   => Success(  HorizontalLineStamp)
    case "vl"   => Success(    VerticalLineStamp)
    case "plus" => Success(            PlusStamp)
    case "star" => Success(            StarStamp)
    case "arr"  => for (dx <- tryToInt(tokens(1)); dy <- tryToInt(tokens(2))) yield ArrowStamp(dx, dy)
    case "tarr" => for (a <- parseArrowTextArrow(tokens(2)); sv <- parseStrokeVal(tokens(3))) yield ArrowTextRectStamp(unquoteString(tokens(1)), a, sv)
    case "t"  => for (sz <- parseFontSize(tokens(2)); f1 <- tryToFloat(tokens(3)); f2 <- tryToFloat(tokens(4))) yield OneTextRectStamp(unquoteString(tokens(1)), sz, f1, f2)
    case "t2" => Success(  TwoTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2))))
    case "t3" => Success(ThreeTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3))))
    case "t4" => Success( FourTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3)), unquoteString(tokens(4))))
    case _ => Failed("RectStamp cannot be parsed from " ++ tokens.toString)
  }
  def stringifyLineStamp(s: LineStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossFixedMark(size, sv) => "x %s %s".format(size.toString, StrokeVal.stringify(sv))
    case CircleFixedMark(size, dv) => "o %s %s".format(size.toString, DrawVal.stringify(dv))
    case SquareFixedMark(size, dv) => "sq %s %s".format(size.toString, DrawVal.stringify(dv))
    case HexagonLineStamp(sv) => "hex %s".format(StrokeVal.stringify(sv))
    case InequalityLineStamp(sv, v) => "i %s".format(StrokeVal.stringify(sv), InequalityLineStamp.stringify(v))
    case StrokeLineStamp(sv) => "s %s".format(StrokeVal.stringify(sv))
    case TransverseLineStamp(sv) => "tv %s".format(StrokeVal.stringify(sv))
    case _ => throw new IllegalArgumentException("LineStamp cannot be stringified: " ++ s.toString)
  }
  def parseLineStamp(tokens: Seq[String]): Status[LineStamp] = tokens(0) match {
    case "clear" => Success(ClearStamp)
    case "x"   => for (sz <- tryToFloat(tokens(1)); sv <- parseStrokeVal(tokens(2))) yield  CrossFixedMark(sz, sv)
    case "o"   => for (sz <- tryToFloat(tokens(1)); dv <- parseDrawVal  (tokens(2))) yield CircleFixedMark(sz, dv)
    case "sq"  => for (sz <- tryToFloat(tokens(1)); dv <- parseDrawVal  (tokens(2))) yield SquareFixedMark(sz, dv)
    case "hex" => for (sv <- parseStrokeVal(tokens(1))) yield HexagonLineStamp(sv)
    case "i"   => for (sv <- parseStrokeVal(tokens(1)); ils <- parseInequality(tokens(2))) yield InequalityLineStamp(sv, ils)
    case "s"   => for (sv <- parseStrokeVal(tokens(1))) yield StrokeLineStamp(sv)
    case "tv"  => for (sv <- parseStrokeVal(tokens(1))) yield TransverseLineStamp(sv)
    case _ => throw new IllegalArgumentException("LineStamp cannot be parsed from " ++ tokens.toString)
  }
  def parseLineStampWithStrokeDefault(tokens: Seq[String]): Status[LineStamp] = {
    tokens match {
      case Seq(tok) => StrokeVal.parse(tokens(0)) match {
        case Some(s) => Success(StrokeLineStamp(s))
        case None => Failed("cannot parse single-element line stamp: " ++ tokens(0))
      }
      case _ => parseLineStamp(tokens)
    }
  }
  def stringifyPointStamp(s: PointStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossFixedMark(size, sv) => "x %s %s".format(size.toString, StrokeVal.stringify(sv))
    case CircleFixedMark(size, dv) => "o %s %s".format(size.toString, DrawVal.stringify(dv))
    case SquareFixedMark(size, dv) => "sq %s %s".format(size.toString, DrawVal.stringify(dv))
    case _ => throw new IllegalArgumentException("PointStamp cannot be stringified: " ++ s.toString)
  }
  def parsePointStamp(tokens: Seq[String]): Status[PointStamp] = tokens(0) match {
    case "clear" => Success(ClearStamp)
    case "x"  => for (sz <- tryToFloat(tokens(1)); sv <- parseStrokeVal(tokens(2))) yield  CrossFixedMark(sz, sv)
    case "o"  => for (sz <- tryToFloat(tokens(1)); dv <- parseDrawVal  (tokens(2))) yield CircleFixedMark(sz, dv)
    case "sq" => for (sz <- tryToFloat(tokens(1)); dv <- parseDrawVal  (tokens(2))) yield SquareFixedMark(sz, dv)
    case _ => Failed("PointStamp cannot be parsed from " ++ tokens.toString)
  }
}
