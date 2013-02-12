package gridderface

import scala.swing.event._

sealed abstract class KeyData
// sort of my own substitute for java's KeyStroke
case class KeyTypedData(char: Char) extends KeyData
case class KeyPressedData(code: Key.Value, mods: Key.Modifiers = 0) extends KeyData
case class KeyReleasedData(code: Key.Value, mods: Key.Modifiers = 0) extends KeyData


object KeyData {
  def extract(ev: KeyEvent) = ev match {
    case KeyTyped(_, char, _, _) => new KeyTypedData(char)
    case KeyPressed(_, code, mods, _) => new KeyPressedData(code, mods)
    case KeyReleased(_, code, mods, _) => new KeyReleasedData(code, mods)
  }
}

