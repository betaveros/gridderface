package gridderface

import gridderface.stamp._
import scala.collection.immutable.HashMap

class WriteSet(val name: String,
  val cellWriteStamp: Option[LineStamp], val intersectionWriteStamp: Option[LineStamp])

object WriteSet {
  val writeSet = new WriteSet("Write", Some(TransverseLineStamp(NormalStrokeVal)), Some(StrokeLineStamp(NormalStrokeVal)))
  val blockSet = new WriteSet("Block", Some(StrokeLineStamp(NormalStrokeVal)), Some(TransverseLineStamp(NormalStrokeVal)))
  val eraseSet = new WriteSet("Erase", Some(ClearStamp), Some(ClearStamp))
  val crossMark = CrossFixedMark(0.125, NormalStrokeVal)
  val crossSet = new WriteSet("Cross", Some(crossMark), Some(crossMark))
  val dashedSet = new WriteSet("Dashed", Some(TransverseLineStamp(NormalDashedStrokeVal)), Some(StrokeLineStamp(NormalDashedStrokeVal)))
  val dotMark = CrossFixedMark(0.125)
  val dotSet = new WriteSet("Dot", Some(dotMark), Some(dotMark))
  val noneSet = new WriteSet("None", None, None)
  val defaultMap: Map[KeyData, WriteSet] = {
    val basicMappings = List(
        KeyTypedData('w') -> writeSet,
        KeyTypedData('b') -> blockSet,
        KeyTypedData('e') -> eraseSet,
        KeyTypedData('x') -> crossSet,
        KeyTypedData('d') -> dashedSet,
        KeyTypedData('D') -> dotSet,
        KeyTypedData('.') -> dotSet,
        KeyTypedData('z') -> noneSet,
        KeyTypedData('n') -> noneSet
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
