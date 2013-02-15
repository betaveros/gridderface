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
  def keyReactions: PartialFunction[KeyData, Unit]
  def mouseReactions: PartialFunction[MouseEvent, Unit]
  def status: String
  def commandPrefixMap: PartialFunction[KeyData, Char]
  def handleCommand(prefix: Char, str: String): Status[String]
}
case class StatusChanged(src: GridderfaceMode) extends Event


