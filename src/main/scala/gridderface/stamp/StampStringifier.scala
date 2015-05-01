package gridderface.stamp

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
    case RectangleArcRectStamp(size, xoff, yoff, dv, tr, br, bl, tl) => "ra %s %s %s %s %s %s %s %s".format(size.toString, xoff.toString, yoff.toString, DrawVal.stringify(dv), sb(tr), sb(br), sb(bl), sb(tl))
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
  def parseRectStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "x"    => BigCrossStamp
    case "smx"  => SmallCrossStamp
    case "mjdg" => MajorDiagonalStamp
    case "mndg" => MinorDiagonalStamp
    case "bigv" => BigCheckStamp
    case "smv" => SmallCheckStamp
    case "f" => FullRectStamp(DrawVal.parse(tokens(1)).get)
    case "o" => CircleRectStamp(tokens(1).toDouble, DrawVal.parse(tokens(2)).get, tokens(3).toDouble, tokens(4).toDouble)
    case "ra" => RectangleArcRectStamp(tokens(1).toDouble, tokens(2).toDouble, tokens(3).toDouble, DrawVal.parse(tokens(4)).get, bs(tokens(5)), bs(tokens(6)), bs(tokens(7)), bs(tokens(8)))
    case "dgf" => DiagonalFillRectStamp
    case "dsf" => DashedFillRectStamp
    case "dtf" => DottedFillRectStamp
    case "hl" => HorizontalLineStamp
    case "vl" => VerticalLineStamp
    case "plus" => PlusStamp
    case "star" => StarStamp
    case "arr" => ArrowStamp(tokens(1).toInt, tokens(2).toInt)
    case "tarr" => ArrowTextRectStamp(unquoteString(tokens(1)), ArrowTextArrow.parse(tokens(2)).get, StrokeVal.parse(tokens(3)).get)
    case "t" => OneTextRectStamp(unquoteString(tokens(1)), OneTextRectStamp.parse(tokens(2)).get, tokens(3).toFloat, tokens(4).toFloat)
    case "t2" => TwoTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)))
    case "t3" => ThreeTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3)))
    case "t4" => FourTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3)), unquoteString(tokens(4)))
    case _ => throw new IllegalArgumentException("RectStamp cannot be parsed from " ++ tokens.toString)
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
  def parseLineStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "x" => CrossFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "o" => CircleFixedMark(tokens(1).toFloat, DrawVal.parse(tokens(2)).get)
    case "sq" => SquareFixedMark(tokens(1).toFloat, DrawVal.parse(tokens(2)).get)
    case "hex" => HexagonLineStamp(StrokeVal.parse(tokens(1)).get)
    case "i" => InequalityLineStamp(StrokeVal.parse(tokens(1)).get, InequalityLineStamp.parse(tokens(2)).get)
    case "s" => StrokeLineStamp(StrokeVal.parse(tokens(1)).get)
    case "tv" => TransverseLineStamp(StrokeVal.parse(tokens(1)).get)
    case _ => throw new IllegalArgumentException("LineStamp cannot be parsed from " ++ tokens.toString)
  }
  def stringifyPointStamp(s: PointStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossFixedMark(size, sv) => "x %s %s".format(size.toString, StrokeVal.stringify(sv))
    case CircleFixedMark(size, dv) => "o %s %s".format(size.toString, DrawVal.stringify(dv))
    case SquareFixedMark(size, dv) => "sq %s %s".format(size.toString, DrawVal.stringify(dv))
    case _ => throw new IllegalArgumentException("PointStamp cannot be stringified: " ++ s.toString)
  }
  def parsePointStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "x" => CrossFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "o" => CircleFixedMark(tokens(1).toFloat, DrawVal.parse(tokens(2)).get)
    case "sq" => SquareFixedMark(tokens(1).toFloat, DrawVal.parse(tokens(2)).get)
    case _ => throw new IllegalArgumentException("PointStamp cannot be parsed from " ++ tokens.toString)
  }
}
