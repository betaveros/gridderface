package gridderface

import java.awt.{ Paint, Color }
import scala.collection.immutable.HashMap
import gridderface.stamp.{ RectStamp, LineStamp, PointStamp, TextRectStamp }
import scala.swing.event.{ MouseEvent, MouseClicked }
import scala.swing.event.MousePressed
import gridderface.stamp.FillRectStamp
import gridderface.stamp.ClearStamp

class GridderfaceViewportMode(panel: GridPanel) extends GridderfaceMode {
  val name = "Viewport"
  private var zoomDegree = 0
  def status = "%2.2f%%".format((panel.getScale() * 100))
  def translate(x: Int, y: Int) = {
    panel.translate(x.toDouble, y.toDouble)
  }

  val zoomReactions: PartialFunction[KeyData, Unit] = k => k match {
    case KeyTypedData('+') => panel.scale(2.0); publish(StatusChanged(this))
    case KeyTypedData('-') => panel.scale(0.5); publish(StatusChanged(this))
  }
  val moveReactions = KeyDataCombinations.keyDataXYFunction(translate)

  def handleCommand(prefix: Char, str: String) = Success("")
  def keyListReactions = new SingletonListPartialFunction(moveReactions orElse
    zoomReactions andThen {u: Unit => KeyComplete})
  val mouseReactions: PartialFunction[MouseEvent, Unit] = Map.empty
  def cursorPaint = SelectedPositionManager.redGrayPaint
}
