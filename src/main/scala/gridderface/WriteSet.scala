package gridderface

import gridderface.stamp._
import scala.collection.immutable.HashMap

class WriteSet(val name: String,
  val cellWriteStamp: Option[LineStamp], val intersectionWriteStamp: Option[LineStamp])

object WriteSet {
  val writeSet = new WriteSet("Write", Some(new TransverseLineStamp(Strokes.normalStroke)), Some(Strokes.normalStamp))
  val blockSet = new WriteSet("Block", Some(Strokes.normalStamp), Some(new TransverseLineStamp(Strokes.normalStroke)))
  val eraseSet = new WriteSet("Erase", Some(ClearStamp), Some(ClearStamp))
  val crossMark = FixedMark.createCrossStamp(0.125, Strokes.normalStroke)
  val crossSet = new WriteSet("Cross", Some(crossMark), Some(crossMark))
  val dashedSet = new WriteSet("Dashed", Some(new TransverseLineStamp(Strokes.normalDashedStroke)), Some(Strokes.normalDashedStamp))
  val dotMark = FixedMark.createDiskStamp(0.125)
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
