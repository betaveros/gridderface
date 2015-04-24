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

  def stringifyRectStamp(s: RectStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossStamp => "X"
    case MajorDiagonalStamp => "\\"
    case MinorDiagonalStamp => "/"
    case CheckStamp => "v"
    case FullRectStamp(dv) => "f %s".format(DrawVal.stringify(dv))
    case CircleRectStamp(size, dv, xoff, yoff) => "O %s %s %s".format(size.toString, DrawVal.stringify(dv), xoff.toString, yoff.toString)
    case DiagonalFillRectStamp => "diagf"
    case DashedFillRectStamp => "dashf"
    case DottedFillRectStamp => "dotf"
    case OneTextRectStamp(s1, fsv, hAlign, vAlign) => "t %s %s %s %s".format(quoteString(s1), OneTextRectStamp.stringify(fsv), hAlign.toString, vAlign.toString)
    case TwoTextRectStamp(s1, s2) => "t2 %s %s".format(quoteString(s1), quoteString(s2))
    case ThreeTextRectStamp(s1, s2, s3) => "t3 %s %s %s".format(quoteString(s1), quoteString(s2), quoteString(s3))
    case FourTextRectStamp(s1, s2, s3, s4) => "t4 %s %s %s %s".format(quoteString(s1), quoteString(s2), quoteString(s3), quoteString(s4))
    case _ => throw new IllegalArgumentException("RectStamp cannot be stringified: " ++ s.toString)
  }
  def parseRectStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "X" => CrossStamp
    case "\\" => MajorDiagonalStamp
    case "/" => MinorDiagonalStamp
    case "v" => CheckStamp
    case "f" => FullRectStamp(DrawVal.parse(tokens(1)).get)
    case "O" => CircleRectStamp(tokens(1).toDouble, DrawVal.parse(tokens(2)).get, tokens(3).toDouble, tokens(4).toDouble)
    case "diagf" => DiagonalFillRectStamp
    case "dashf" => DashedFillRectStamp
    case "dotf" => DottedFillRectStamp
    case "t" => OneTextRectStamp(unquoteString(tokens(1)), OneTextRectStamp.parse(tokens(2)).get, tokens(3).toFloat, tokens(4).toFloat)
    case "t2" => TwoTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)))
    case "t3" => ThreeTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3)))
    case "t4" => FourTextRectStamp(unquoteString(tokens(1)), unquoteString(tokens(2)), unquoteString(tokens(3)), unquoteString(tokens(4)))
    case _ => throw new IllegalArgumentException("RectStamp cannot be parsed from " ++ tokens.toString)
  }
  def stringifyLineStamp(s: LineStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossFixedMark(size, sv) => "x %s %s".format(size.toString, StrokeVal.stringify(sv))
    case CircleFixedMark(size, sv) => "o %s %s".format(size.toString, StrokeVal.stringify(sv))
    case DiskFixedMark(size) => "disk %s".format(size.toString)
    case FilledSquareFixedMark(size) => "fsq %s".format(size.toString)
    case HexagonLineStamp(sv) => "hex %s".format(StrokeVal.stringify(sv))
    case InequalityLineStamp(sv, v) => "i %s".format(StrokeVal.stringify(sv), InequalityLineStamp.stringify(v))
    case StrokeLineStamp(sv) => "s %s".format(StrokeVal.stringify(sv))
    case TransverseLineStamp(sv) => "tv %s".format(StrokeVal.stringify(sv))
    case _ => throw new IllegalArgumentException("LineStamp cannot be stringified: " ++ s.toString)
  }
  def parseLineStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "x" => CrossFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "o" => CircleFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "disk" => DiskFixedMark(tokens(1).toFloat)
    case "fsq" => FilledSquareFixedMark(tokens(1).toFloat)
    case "hex" => HexagonLineStamp(StrokeVal.parse(tokens(1)).get)
    case "i" => InequalityLineStamp(StrokeVal.parse(tokens(1)).get, InequalityLineStamp.parse(tokens(2)).get)
    case "s" => StrokeLineStamp(StrokeVal.parse(tokens(1)).get)
    case "tv" => TransverseLineStamp(StrokeVal.parse(tokens(2)).get)
    case _ => throw new IllegalArgumentException("LineStamp cannot be parsed from " ++ tokens.toString)
  }
  def stringifyPointStamp(s: PointStamp) = s match {
    case ClearStamp => "clear" // this shouldn't be used
    case CrossFixedMark(size, sv) => "x %s %s".format(size.toString, StrokeVal.stringify(sv))
    case CircleFixedMark(size, sv) => "o %s %s".format(size.toString, StrokeVal.stringify(sv))
    case DiskFixedMark(size) => "disk %s".format(size.toString)
    case FilledSquareFixedMark(size) => "fsq %s".format(size.toString)
    case _ => throw new IllegalArgumentException("PointStamp cannot be stringified: " ++ s.toString)
  }
  def parsePointStamp(tokens: Seq[String]) = tokens(0) match {
    case "clear" => ClearStamp
    case "x" => CrossFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "o" => CircleFixedMark(tokens(1).toFloat, StrokeVal.parse(tokens(2)).get)
    case "disk" => DiskFixedMark(tokens(1).toFloat)
    case "fsq" => FilledSquareFixedMark(tokens(1).toFloat)
    case _ => throw new IllegalArgumentException("PointStamp cannot be parsed from " ++ tokens.toString)
  }
}
