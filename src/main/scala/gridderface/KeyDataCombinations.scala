package gridderface

import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped

object KeyDataCombinations {
  def keyDataXYFunction[B](func: (Int, Int) => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData('h') => func(-1, 0)
    case KeyTypedData('j') => func(0, 1)
    case KeyTypedData('k') => func(0, -1)
    case KeyTypedData('l') => func(1, 0)
  }
  def keyDataRCFunction[B](func: (Int, Int) => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData('h') => func(0, -1)
    case KeyTypedData('j') => func(1, 0)
    case KeyTypedData('k') => func(-1, 0)
    case KeyTypedData('l') => func(0, 1)
  }
  def keyDataDigitFunction[B](func: Int => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData(c) if '0' <= c && c <= '9' => func(c - '0')
  }
}