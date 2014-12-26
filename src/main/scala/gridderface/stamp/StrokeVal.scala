package gridderface.stamp
import java.awt._

sealed abstract class StrokeVal {
  def stroke: Stroke
}
import StrokeVal._
case object NormalStrokeVal extends StrokeVal { val stroke = new BasicStroke(normalWidth) }
case object MediumStrokeVal extends StrokeVal { val stroke = new BasicStroke(mediumWidth) }
case object ThickStrokeVal  extends StrokeVal { val stroke = new BasicStroke(thickWidth ) }
case object ThinStrokeVal   extends StrokeVal { val stroke = new BasicStroke(thinWidth  ) }
case object NormalDashedStrokeVal extends StrokeVal { val stroke = createDashedStroke(normalWidth, 1.0f / 6.0f) }
case object ThinDashedStrokeVal   extends StrokeVal { val stroke = createDashedStroke(thinWidth  , 0.125f) }

object StrokeVal {
  val normalWidth = 0.125f
  val thickWidth = 0.25f
  val thinWidth = 0.0625f

  // experimentally chosen as Slitherlink-flood-fill edge width
  val mediumWidth = 0.1875f

  def createDashedStroke(width: Float, dash: Float, gap: Float) =
    new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
        Array(dash, gap), 0.0f)
  def createDashedStroke(width: Float, dash: Float): BasicStroke =
    createDashedStroke(width, dash, dash)
  private val strokeValStringCorrespondences: Seq[(StrokeVal, String)] = Seq(
    NormalStrokeVal -> "n",
    MediumStrokeVal -> "m",
    ThickStrokeVal -> "t",
    ThinStrokeVal -> "s",
    NormalDashedStrokeVal -> "nd",
    ThinDashedStrokeVal -> "d"
  )
  private val strokeValStringMap: Map[StrokeVal, String] = Map(strokeValStringCorrespondences: _*)
  private val stringStrokeValMap: Map[String, StrokeVal] = Map(strokeValStringCorrespondences map (_.swap): _*)
  def parse(s: String): Option[StrokeVal] = stringStrokeValMap get s
  def stringify(s: StrokeVal): String = strokeValStringMap(s)
}
