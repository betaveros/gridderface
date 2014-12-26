package gridderface

import gridderface.stamp._
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

class StampSet(val name: String,
  val rectStamp: Option[RectStamp], val lineStamp: Option[LineStamp], val pointStamp: Option[PointStamp])

object StampSet {
  val fillSet = new StampSet(
    "Fill", Some(FillRectStamp), Some(Strokes.normalStamp), Some(FixedMark.createFilledSquareStamp(0.125)))
  val thickSet = new StampSet(
    "10/Thick", Some(new OneTextRectStamp("10")), Some(Strokes.thickStamp), Some(FixedMark.createFilledSquareStamp(0.25)))
  val mediumSet = new StampSet(
    "Medium", None, Some(Strokes.mediumStamp), Some(FixedMark.createFilledSquareStamp(0.1875)))
  val shadefillSet = new StampSet(
    "shadeFill", Some(TextureFillRectStamp.diagonalStamp), Some(Strokes.thinStamp), Some(FixedMark.createFilledSquareStamp(0.125)))
  val dashSet = new StampSet(
    "Dash", Some(TextureFillRectStamp.dashedStamp), Some(Strokes.normalDashedStamp), Some(FixedMark.createDiskStamp(0.125)))
  val dotSet = new StampSet(
    "Dot", Some(new BulbRectStamp(0.25)), Some(FixedMark.createDiskStamp(0.125)), Some(FixedMark.createDiskStamp(0.125)))
  val cornerDotSet = new StampSet(
    "CornerDot", Some(new BulbRectStamp(0.25, 0.25, 0.25)), Some(FixedMark.createDiskStamp(0.125)), Some(FixedMark.createDiskStamp(0.125)))
  val circleSet = new StampSet(
    "Circle", Some(new CircleRectStamp(0.75)), Some(FixedMark.createCircleStamp(0.125)), Some(FixedMark.createCircleStamp(0.125)))
  val eSet = new StampSet(
    "11/Trans", Some(new OneTextRectStamp("11")), Some(new TransverseLineStamp(Strokes.normalStroke)), None)
  val clearSet = new StampSet(
    "Clear", Some(ClearStamp), Some(ClearStamp), Some(ClearStamp))
  val crossMark = FixedMark.createCrossStamp(0.125, Strokes.normalStroke)
  val slash1Set = new StampSet(
    "\\", Some(PathRectStamp.slash1Stamp), None, None)
  val slash2Set = new StampSet(
    "/", Some(PathRectStamp.slash2Stamp), None, None)
  val crossSet = new StampSet(
    "Cross", Some(PathRectStamp.crossStamp), Some(crossMark), Some(crossMark))
  val checkSet = new StampSet(
    "Check", Some(PathRectStamp.checkStamp), None, None)
  val lessSet = new StampSet(
    "LessThan", None, Some(new InequalityLineStamp(Strokes.normalStroke, true)), None)
  val greaterSet = new StampSet(
    "GreaterThan", None, Some(new InequalityLineStamp(Strokes.normalStroke, false)), None)
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
