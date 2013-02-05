package gridderface

import java.awt.Paint
import java.awt.Color
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

class PaintSet(val name: String, val paint: Paint) {

}

object BuiltinPaints {
  val defaultMap: HashMap[KeyData, PaintSet] = HashMap(
    KeyTypedData('a') -> new PaintSet("Black", Color.BLACK),
    KeyTypedData('r') -> new PaintSet("Red", new Color(255, 0, 0)),
    KeyTypedData('g') -> new PaintSet("Green", new Color(0, 192, 0)),
    KeyTypedData('b') -> new PaintSet("Blue", new Color(0, 0, 255)),
    KeyTypedData('c') -> new PaintSet("Cyan", new Color(0, 192, 192)),
    KeyTypedData('m') -> new PaintSet("Magenta", new Color(192, 0, 192)),
    KeyTypedData('y') -> new PaintSet("Yellow", new Color(192, 192, 0)),
    KeyTypedData('A') -> new PaintSet("Gray", new Color(192, 192, 192)),
    KeyTypedData('R') -> new PaintSet("LightRed", new Color(255, 192, 192)),
    KeyTypedData('G') -> new PaintSet("LightGreen", new Color(128, 255, 128)),
    KeyTypedData('B') -> new PaintSet("LightBlue", new Color(192, 192, 255)),
    KeyTypedData('C') -> new PaintSet("LightCyan", new Color(128, 255, 255)),
    KeyTypedData('M') -> new PaintSet("LightMagenta", new Color(255, 128, 255)),
    KeyTypedData('Y') -> new PaintSet("LightYellow", new Color(255, 255, 128)),
    KeyTypedData('w') -> new PaintSet("White", Color.WHITE)
    
  )
}