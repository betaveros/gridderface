package gridderface

import scala.swing.event._

sealed abstract class KeyData {
  def toKeyString: String
}
// sort of my own substitute for java's KeyStroke
case class KeyTypedData(char: Char) extends KeyData {
  def toKeyString = char match {
    case c if c < ' ' => "<^" + (c + '@').toChar.toString + ">"
    case ' ' => "<Space>"
    case '<' => "<LT>"
    case '>' => "<GT>"
    case _ => char.toString
  }
}
case class KeyPressedData(code: Key.Value, mods: Key.Modifiers = 0) extends KeyData {
  def toKeyString = {
    val txt = java.awt.event.KeyEvent.getKeyModifiersText(mods)
    "<" + (if (txt.length > 0) txt + "-" else "") + KeyData.codeToString(code) + ">"
  }
}
case class KeyReleasedData(code: Key.Value, mods: Key.Modifiers = 0) extends KeyData {
  def toKeyString = {
    val txt = java.awt.event.KeyEvent.getKeyModifiersText(mods)
    "</ " + (if (txt.length > 0) txt + "-" else "") + KeyData.codeToString(code) + ">"
  }
}

object KeyData {
  def extract(ev: KeyEvent) = ev match {
    case KeyTyped(_, char, _, _) => new KeyTypedData(char)
    case KeyPressed(_, code, mods, _) => new KeyPressedData(code, mods)
    case KeyReleased(_, code, mods, _) => new KeyReleasedData(code, mods)
  }
  def codeToString(code: Key.Value) = code match {
    case Key.Tab => "Tab"
    case _ => code.toString
  }
}

