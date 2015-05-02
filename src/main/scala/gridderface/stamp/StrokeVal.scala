package gridderface.stamp
import java.awt._

sealed abstract class StrokeVal {
  def stroke: Stroke
  def roundStroke: Stroke
}
import StrokeVal._

abstract class BasicStrokeVal(strokeWidth: Float) extends StrokeVal {
  val stroke = new BasicStroke(strokeWidth)
  val roundStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
}
case object NormalStrokeVal extends BasicStrokeVal(normalWidth)
case object MediumStrokeVal extends BasicStrokeVal(mediumWidth)
case object ThickStrokeVal  extends BasicStrokeVal(thickWidth )
case object ThinStrokeVal   extends BasicStrokeVal(thinWidth  )

abstract class DashedStrokeVal(strokeWidth: Float, strokeDash: Float) extends StrokeVal {
  val stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
        Array(strokeDash, strokeDash), 0.0f)
  val roundStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
        Array(strokeDash, strokeDash), 0.0f)
}
case object NormalDashedStrokeVal extends DashedStrokeVal(normalWidth, 1.0f / 6.0f)
case object ThinDashedStrokeVal   extends DashedStrokeVal(thinWidth  , 0.125f)

object StrokeVal {
  val normalWidth = 0.125f
  val thickWidth = 0.25f
  val thinWidth = 0.0625f

  // experimentally chosen as Slitherlink-flood-fill edge width
  val mediumWidth = 0.1875f

  // ok so there's some initialization order wonkiness here
  // somehow, without lazy, ThickStrokeVal is null here
  private lazy val strokeValStringCorrespondences: Seq[(StrokeVal, String)] = Seq(
    NormalStrokeVal -> "n",
    MediumStrokeVal -> "m",
    ThickStrokeVal -> "t",
    ThinStrokeVal -> "s",
    NormalDashedStrokeVal -> "nd",
    ThinDashedStrokeVal -> "d"
  )
  private lazy val strokeValStringMap: Map[StrokeVal, String] = Map(strokeValStringCorrespondences: _*)
  private lazy val stringStrokeValMap: Map[String, StrokeVal] = Map(strokeValStringCorrespondences map (_.swap): _*)
  def parse(s: String): Option[StrokeVal] = stringStrokeValMap get s
  def stringify(s: StrokeVal): String = strokeValStringMap(s)
}
