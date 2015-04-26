package gridderface

import gridderface.stamp._
import scala.collection.immutable.HashMap
import java.awt.Paint

abstract class WriteSet(val name: String) {
  def         cellWrite(currentPaint: Paint, oldContent: Option[LineContent]): LineContent
  def intersectionWrite(currentPaint: Paint, oldContent: Option[LineContent]): LineContent
}

class PlainWriteSet(name: String, val cellLineStamp: LineStamp, val intersectionLineStamp: LineStamp) extends WriteSet(name) {
  def         cellWrite(currentPaint: Paint, oldContent: Option[LineContent]) = new LineStampContent(        cellLineStamp, currentPaint)
  def intersectionWrite(currentPaint: Paint, oldContent: Option[LineContent]) = new LineStampContent(intersectionLineStamp, currentPaint)
}
class TwiceWriteSet(name: String, val cellLineStamp: LineStamp,         val alternateCellLineStamp: LineStamp,
                          val intersectionLineStamp: LineStamp, val alternateIntersectionLineStamp: LineStamp) extends WriteSet(name) {
  def cellWrite(currentPaint: Paint, oldContent: Option[LineContent]) = {
    oldContent match {
      case Some(LineStampContent(stamp, _)) if stamp == cellLineStamp => new LineStampContent(alternateCellLineStamp, currentPaint)
      case _ => new LineStampContent(cellLineStamp, currentPaint)
    }
  }
  def intersectionWrite(currentPaint: Paint, oldContent: Option[LineContent]) = {
    oldContent match {
      case Some(LineStampContent(stamp, _)) if stamp == intersectionLineStamp => new LineStampContent(alternateIntersectionLineStamp, currentPaint)
      case _ => new LineStampContent(intersectionLineStamp, currentPaint)
    }
  }
}
object NoneSet extends WriteSet("None") {
  def         cellWrite(currentPaint: Paint, oldContent: Option[LineContent]) = oldContent getOrElse new LineStampContent(ClearStamp, currentPaint)
  def intersectionWrite(currentPaint: Paint, oldContent: Option[LineContent]) = oldContent getOrElse new LineStampContent(ClearStamp, currentPaint)
}

object WriteSet {
  def plain2(name: String, lineStamp: LineStamp) = new PlainWriteSet(name, lineStamp, lineStamp)
  val writeSet  = new PlainWriteSet("Write" , TransverseLineStamp(      NormalStrokeVal),     StrokeLineStamp(      NormalStrokeVal))
  val blockSet  = new PlainWriteSet("Block" ,     StrokeLineStamp(      NormalStrokeVal), TransverseLineStamp(      NormalStrokeVal))
  val dashedSet = new PlainWriteSet("Dashed", TransverseLineStamp(NormalDashedStrokeVal),     StrokeLineStamp(NormalDashedStrokeVal))
  val crossMark = CrossFixedMark(0.125, NormalStrokeVal)
  val crossSet  = plain2("Cross", crossMark)
  val eraseSet  = plain2("Erase", ClearStamp)
  val dotSet    = plain2("Dot", DiskFixedMark(0.125))
  val twiceWriteSet      = new TwiceWriteSet("Write <> Clear" , TransverseLineStamp(NormalStrokeVal), ClearStamp, StrokeLineStamp(NormalStrokeVal), ClearStamp)
  val twiceWriteCrossSet = new TwiceWriteSet("Write <> Cross" , TransverseLineStamp(NormalStrokeVal),  crossMark, StrokeLineStamp(NormalStrokeVal),  crossMark)
  val defaultMap: Map[KeyData, WriteSet] = {
    val basicMappings = List(
        KeyTypedData('w') -> writeSet,
        KeyTypedData('b') -> blockSet,
        KeyTypedData('e') -> eraseSet,
        KeyTypedData('x') -> crossSet,
        KeyTypedData('d') -> dashedSet,
        KeyTypedData('D') -> dotSet,
        KeyTypedData('.') -> dotSet,
        KeyTypedData('t') -> twiceWriteSet,
        KeyTypedData('X') -> twiceWriteCrossSet,
        KeyTypedData('z') -> NoneSet,
        KeyTypedData('n') -> NoneSet
      )
    HashMap(basicMappings: _*)
  }

  //    case KeyTyped(_, 'f', _, _) => fillSet
  //    case KeyTyped(_, 't', _, _) => thickSet
  //    case KeyTyped(_, ' ', _, _) => clearSet
  //    case KeyTyped(_, 'x', _, _) => crossSet
  //    case KeyTyped(_, c, _, _) if '0' <= c && c <= '9' => digitSets(c - '0')
  //  }
}
