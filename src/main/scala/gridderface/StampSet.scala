package gridderface

import gridderface.stamp._
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

case class StampSet(val name: String,
  val rectStamp: Option[RectStamp], val lineStamp: Option[LineStamp], val pointStamp: Option[PointStamp])

object StampSet {
  val fillSet = StampSet(
    "Fill", Some(FullRectStamp(Fill)), Some(StrokeLineStamp(NormalStrokeVal)), Some(SquareFixedMark(0.125, Fill)))
  val thickSet = StampSet(
    "10/Thick", Some(new OneTextRectStamp("10")), Some(StrokeLineStamp(ThickStrokeVal)), Some(SquareFixedMark(0.25, Fill)))
  val mediumSet = StampSet(
    "Medium", Some(RectangleArcRectStamp(0.75, Fill, false, false, false, false)), Some(StrokeLineStamp(MediumStrokeVal)), Some(SquareFixedMark(0.1875, Fill)))
  val shadefillSet = StampSet(
    "shadeFill", Some(DiagonalFillRectStamp), Some(StrokeLineStamp(ThinStrokeVal)), Some(SquareFixedMark(0.125, Fill)))
  val dashSet = StampSet(
    "Dash", Some(DashedFillRectStamp), Some(StrokeLineStamp(NormalDashedStrokeVal)), Some(CircleFixedMark(0.125, Fill)))
  val thinDashSet = StampSet(
    "thinDash", Some(DashedFillRectStamp), Some(StrokeLineStamp(ThinDashedStrokeVal)), Some(CircleFixedMark(0.125, Fill)))
  val dotSet = StampSet(
    "Dot", Some(CircleRectStamp(0.25, Fill)), Some(CircleFixedMark(0.125, Fill)), Some(CircleFixedMark(0.125, Fill)))
  val cornerDotSet = StampSet(
    "CornerDot", Some(CircleRectStamp(0.25, Fill, 0.25, 0.25)), Some(CircleFixedMark(0.125, Fill)), Some(CircleFixedMark(0.125, Draw(ThinStrokeVal))))
  val circleSet = StampSet(
    "Circle", Some(CircleRectStamp(0.6875)), Some(CircleFixedMark(0.125)), Some(CircleFixedMark(0.25, Draw(ThinStrokeVal))))
  val bulbSet = StampSet(
    "Bulb", Some(CircleRectStamp(0.75, Fill)), Some(CircleFixedMark(0.125, Fill)), Some(CircleFixedMark(0.28125, Fill)))
  val starSet = StampSet(
    "Star", Some(StarStamp), None, None)
  val eSet = StampSet(
    "11/Trans", Some(OneTextRectStamp("11")), Some(TransverseLineStamp(NormalStrokeVal)), None)
  val clearSet = StampSet(
    "Clear", Some(ClearStamp), Some(ClearStamp), Some(ClearStamp))
  val crossMark = CrossFixedMark(0.125, NormalStrokeVal)
  val smallCrossMark = CrossFixedMark(0.125, ThinStrokeVal)
  val slash1Set = StampSet(
    "\\", Some(MajorDiagonalStamp), None, None)
  val slash2Set = StampSet(
    "/", Some(MinorDiagonalStamp), None, None)
  val horizontalSet = StampSet(
    "-", Some(HorizontalLineStamp), None, None)
  val verticalSet = StampSet(
    "|", Some(VerticalLineStamp), None, None)
  val plusSet = StampSet(
    "+", Some(PlusStamp), None, None)
  val bigCrossSet = StampSet(
    "Cross", Some(BigCrossStamp), Some(crossMark), Some(crossMark))
  val smallCrossSet = StampSet(
    "S.Cross", Some(SmallCrossStamp), Some(smallCrossMark), Some(smallCrossMark))
  val bigCheckSet = StampSet(
    "Check", Some(BigCheckStamp), None, None)
  val smallCheckSet = StampSet(
    "S.Check", Some(SmallCheckStamp), None, None)
  val lessSet = StampSet(
    "LessThan", None, Some(InequalityLineStamp(NormalStrokeVal, InequalityLineStamp.Less)), None)
  val greaterSet = StampSet(
    "GreaterThan", None, Some(InequalityLineStamp(NormalStrokeVal, InequalityLineStamp.Greater)), None)
  val leftShipSet = StampSet(
    "LeftShip",  Some(RectangleArcRectStamp(0.75, Fill, false, false, true , true )), None, None)
  val rightShipSet = StampSet(
    "RightShip", Some(RectangleArcRectStamp(0.75, Fill, true , true , false, false)), None, None)
  val upShipSet = StampSet(
    "UpShip",    Some(RectangleArcRectStamp(0.75, Fill, true , false, false, true )), None, None)
  val downShipSet = StampSet(
    "DownShip",  Some(RectangleArcRectStamp(0.75, Fill, false, true , true , false)), None, None)
  val questionSet = StampSet(
    "?", Some(OneTextRectStamp("?")), None, None)
  val bangSet = StampSet(
    "!", Some(OneTextRectStamp("!")), None, None)

  def charMappings(cs: Seq[Char]): Seq[(KeyData, StampSet)] = cs map (c => KeyTypedData(c) ->
    new StampSet("'" + c + "'", Some(new OneTextRectStamp(c.toString)), None, None))

  val digitMappings = charMappings('0' to '9')
  val letterMappings = charMappings('A' to 'Z')

  val defaultMap: Map[KeyData, StampSet] = {
    val basicMappings = List(KeyTypedData('f') -> fillSet,
        KeyTypedData('F') -> shadefillSet,
        KeyTypedData('d') -> dashSet,
        KeyTypedData('D') -> thinDashSet,
        KeyTypedData('s') -> mediumSet,
        KeyTypedData('.') -> dotSet,
        KeyTypedData(',') -> cornerDotSet,
        KeyTypedData('o') -> bulbSet,
        KeyTypedData('O') -> circleSet,
        KeyTypedData('b') -> bulbSet,
        KeyTypedData('e') -> eSet,
        KeyTypedData('t') -> thickSet,
        KeyTypedData('\\') -> slash1Set,
        KeyTypedData('/') -> slash2Set,
        KeyTypedData('-') -> horizontalSet,
        KeyTypedData('|') -> verticalSet,
        KeyTypedData('+') -> plusSet,
        KeyTypedData('v') -> bigCheckSet,
        KeyTypedData('V') -> smallCheckSet,
        KeyTypedData('<') -> lessSet,
        KeyTypedData('>') -> greaterSet,
        KeyTypedData('(') -> leftShipSet,
        KeyTypedData(')') -> rightShipSet,
        KeyTypedData('n') -> upShipSet,
        KeyTypedData('u') -> downShipSet,
        KeyTypedData('?') -> questionSet,
        KeyTypedData('!') -> bangSet,
        KeyTypedData('*') -> starSet,
        KeyTypedData(' ') -> clearSet,
        KeyTypedData('x') -> bigCrossSet,
        KeyTypedData('X') -> smallCrossSet
      )
    HashMap((basicMappings ++ digitMappings): _*)
  }
  val alphaMap: Map[KeyData, StampSet] = defaultMap ++ letterMappings

  //    case KeyTyped(_, 'f', _, _) => fillSet
  //    case KeyTyped(_, 't', _, _) => thickSet
  //    case KeyTyped(_, ' ', _, _) => clearSet
  //    case KeyTyped(_, 'x', _, _) => crossSet
  //    case KeyTyped(_, c, _, _) if '0' <= c && c <= '9' => digitSets(c - '0')
  //  }
}
