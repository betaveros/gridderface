package gridderface

import scala.swing._
import scala.swing.event._
import java.awt.Font
import java.awt.Color

class CommandLinePanel(val responder: (Char, String) => Either[String, String]) extends BoxPanel(Orientation.Horizontal) {
  private val prefixLabel = new Label
  private val field = new TextField
  field.font = new Font("Monospaced", Font.PLAIN, 20)
  prefixLabel.font = new Font("Monospaced", Font.PLAIN, 20)
  private var prefix = '\0'
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
  field.listenTo(field.keys)
  field.reactions += {
    case _: FocusLost => stopCommandMode
    case KeyPressed(_, Key.Escape, _, _) => stopCommandMode
    case KeyPressed(_, Key.OpenBracket, Key.Modifier.Control, _) => stopCommandMode
    case KeyPressed(_, Key.BackSpace, _, _) => {
      if (field.text.length == 0) stopCommandMode
    }
    case KeyPressed(_, Key.Enter, _, _) => {
      val message = responder(prefix, field.text)
      stopCommandMode
      message match {
        case Left(text) => field.background = errorColor; field.text = text
        case Right(text) => field.text = text
      }
    }
  }
  
  contents += prefixLabel
  contents += field
  stopCommandMode()
  
}