package gridderface

import gridderface.stamp._
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

class StampSet(val name: String,
  val rectStamp: Option[RectStamp], val lineStamp: Option[LineStamp], val pointStamp: Option[PointStamp])

object StampSet {
  val fillSet = new StampSet(
    "Fill", Some(FillRectStamp), Some(StrokeLineStamp(NormalStrokeVal)), Some(FilledSquareFixedMark(0.125)))
  val thickSet = new StampSet(
    "10/Thick", Some(new OneTextRectStamp("10")), Some(StrokeLineStamp(ThickStrokeVal)), Some(FilledSquareFixedMark(0.25)))
  val mediumSet = new StampSet(
    "Medium", None, Some(StrokeLineStamp(MediumStrokeVal)), Some(FilledSquareFixedMark(0.1875)))
  val shadefillSet = new StampSet(
    "shadeFill", Some(DiagonalFillRectStamp), Some(StrokeLineStamp(ThinStrokeVal)), Some(FilledSquareFixedMark(0.125)))
  val dashSet = new StampSet(
    "Dash", Some(DashedFillRectStamp), Some(StrokeLineStamp(NormalDashedStrokeVal)), Some(DiskFixedMark(0.125)))
  val dotSet = new StampSet(
    "Dot", Some(BulbRectStamp(0.25)), Some(DiskFixedMark(0.125)), Some(DiskFixedMark(0.125)))
  val cornerDotSet = new StampSet(
    "CornerDot", Some(BulbRectStamp(0.25, 0.25, 0.25)), Some(DiskFixedMark(0.125)), Some(DiskFixedMark(0.125)))
  val circleSet = new StampSet(
    "Circle", Some(CircleRectStamp(0.75)), Some(CircleFixedMark(0.125)), Some(CircleFixedMark(0.125)))
  val eSet = new StampSet(
    "11/Trans", Some(OneTextRectStamp("11")), Some(TransverseLineStamp(NormalStrokeVal)), None)
  val clearSet = new StampSet(
    "Clear", Some(ClearStamp), Some(ClearStamp), Some(ClearStamp))
  val crossMark = CrossFixedMark(0.125, NormalStrokeVal)
  val slash1Set = new StampSet(
    "\\", Some(MajorDiagonalStamp), None, None)
  val slash2Set = new StampSet(
    "/", Some(MinorDiagonalStamp), None, None)
  val crossSet = new StampSet(
    "Cross", Some(CrossStamp), Some(crossMark), Some(crossMark))
  val checkSet = new StampSet(
    "Check", Some(CheckStamp), None, None)
  val lessSet = new StampSet(
    "LessThan", None, Some(InequalityLineStamp(NormalStrokeVal, InequalityLineStamp.Less)), None)
  val greaterSet = new StampSet(
    "GreaterThan", None, Some(InequalityLineStamp(NormalStrokeVal, InequalityLineStamp.Greater)), None)
  val defaultMap: Map[KeyData, StampSet] = {
    val basicMappings = List(KeyTypedData('f') -> fillSet,
        KeyTypedData('F') -> shadefillSet,
        KeyTypedData('d') -> dashSet,
        KeyTypedData('D') -> dotSet,
        KeyTypedData('s') -> mediumSet,
        KeyTypedData('.') -> dotSet,
        KeyTypedData(',') -> cornerDotSet,
        KeyTypedData('o') -> circleSet,
        KeyTypedData('e') -> eSet,
        KeyTypedData('t') -> thickSet,
        KeyTypedData('\\') -> slash1Set,
        KeyTypedData('/') -> slash2Set,
        KeyTypedData('v') -> checkSet,
        KeyTypedData('<') -> lessSet,
        KeyTypedData('>') -> greaterSet,
        KeyTypedData(' ') -> clearSet,
        KeyTypedData('x') -> crossSet)
    val digitMappings = (0 to 9) map (i => KeyTypedData(('0' + i).toChar) ->
      new StampSet("'" + i + "'", Some(new OneTextRectStamp("" + i)), None, None))
    HashMap((basicMappings ++ digitMappings): _*)
  }

  //    case KeyTyped(_, 'f', _, _) => fillSet
  //    case KeyTyped(_, 't', _, _) => thickSet
  //    case KeyTyped(_, ' ', _, _) => clearSet
  //    case KeyTyped(_, 'x', _, _) => crossSet
  //    case KeyTyped(_, c, _, _) if '0' <= c && c <= '9' => digitSets(c - '0')
  //  }
}
