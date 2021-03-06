package gridderface

import java.awt.Color
import gridderface.stamp._
import java.awt.Paint
import scala.collection.immutable.HashMap
import scala.swing.event.MouseEvent
import scala.swing.event.MouseClicked
import scala.swing.Publisher
import scala.swing.event.Event

trait GridderfaceMode extends Publisher {
  def name: String
  def keyListReactions: PartialFunction[List[KeyData], KeyResult]
  def mouseReactions: PartialFunction[MouseEvent, Unit]
  def status: String
  def handleCommand(prefix: Char, str: String): Status[String]
  def handleColonCommand(command: String, args: Array[String]): Status[String] =
    Failed("unrecognized command: " + command)
  def cursorPaint: Paint
}
case class StatusChanged(src: Any) extends Event
