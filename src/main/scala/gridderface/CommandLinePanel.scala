package gridderface

import scala.swing._
import scala.swing.event._
import java.awt.Font
import java.awt.Color

class CommandLinePanel(val responder: (Char, String) => Status[String]) extends BoxPanel(Orientation.Horizontal) {
  private val prefixLabel = new Label
  private val field = new TextField
  field.font = new Font("Monospaced", Font.PLAIN, 20)
  field.peer setFocusTraversalKeysEnabled false // prevent tab key from being consumed
  prefixLabel.font = new Font("Monospaced", Font.PLAIN, 20)
  private var prefix = '0' // whatever
  val enabledColor = new Color(192, 255, 255)
  val disabledColor = new Color(192, 192, 192)
  val errorColor = new Color(255, 160, 160)
  def startCommandMode(p: Char) {
    prefix = p
    prefixLabel.text = prefix.toString
    field.text = ""
    field.enabled = true
    field.background = enabledColor
    field.requestFocusInWindow
  }
  def stopCommandMode() {
    prefixLabel.text = ""
    field.text = ""
    field.enabled = false
    field.background = disabledColor
  }
  def showMessage(msg: String, isError: Boolean = false) {
    field.background = if (isError) errorColor else
      if (field.enabled) enabledColor else disabledColor
    field.text = msg
  }
  def showStatus(stat: Status[String]) {
    stat match {
      case Failed(text) => showMessage("Error: " + text, true)
      case Success(text) => showMessage(text, false)
    }
  }
  def showError(msg: String) = showMessage(msg, true)
  field.listenTo(field.keys)
  field.reactions += {
    case _: FocusLost => stopCommandMode
    case KeyPressed(_, Key.Escape, _, _) => stopCommandMode
    case KeyPressed(_, Key.OpenBracket, Key.Modifier.Control, _) => stopCommandMode
    case KeyPressed(_, Key.Tab, _, _) => field.text = field.text match {
      // this is just a mock-up, don't take it too seriously
      case "w" => "write "
      case "q" => "quit"
      case "d" => "dump"
      case "n" => "newlayer"
      case "dec pre s" => "dec pre slitherlink"
      case "dec pre f" => "dec pre fillomino"
      case "dec pre n" => "dec pre nurikabe"
      case s => s
    }
    case KeyPressed(_, Key.BackSpace, _, _) => {
      if (field.text.length == 0) stopCommandMode
    }
    case KeyPressed(_, Key.Enter, _, _) => {
      val stat = responder(prefix, field.text)
      stopCommandMode
      showStatus(stat)
    }
  }

  contents += prefixLabel
  contents += field
  stopCommandMode()
}
