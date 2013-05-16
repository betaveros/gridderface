package gridderface

import scala.swing.event.{ KeyEvent, KeyTyped, Key }

object KeyDataCombinations {
  def keyDataXYFunction[B](func: (Int, Int) => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData('h') => func(-1, 0)
    case KeyTypedData('j') => func(0, 1)
    case KeyTypedData('k') => func(0, -1)
    case KeyTypedData('l') => func(1, 0)
    case KeyPressedData(Key.Left , 0) => func(-1, 0)
    case KeyPressedData(Key.Down , 0) => func(0, 1)
    case KeyPressedData(Key.Up   , 0) => func(0, -1)
    case KeyPressedData(Key.Right, 0) => func(1, 0)
  }
  def keyDataRCFunction[B](func: (Int, Int) => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData('h') => func(0, -1)
    case KeyTypedData('j') => func(1, 0)
    case KeyTypedData('k') => func(-1, 0)
    case KeyTypedData('l') => func(0, 1)
    case KeyPressedData(Key.Left , 0) => func(0, -1)
    case KeyPressedData(Key.Down , 0) => func(1, 0)
    case KeyPressedData(Key.Up   , 0) => func(-1, 0)
    case KeyPressedData(Key.Right, 0) => func(0, 1)
  }
  def keyDataShiftRCFunction[B](func: (Int, Int) => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData('H') => func(0, -1)
    case KeyTypedData('J') => func(1, 0)
    case KeyTypedData('K') => func(-1, 0)
    case KeyTypedData('L') => func(0, 1)
    case KeyPressedData(Key.Left , Key.Modifier.Shift) => func(0, -1)
    case KeyPressedData(Key.Down , Key.Modifier.Shift) => func(1, 0)
    case KeyPressedData(Key.Up   , Key.Modifier.Shift) => func(-1, 0)
    case KeyPressedData(Key.Right, Key.Modifier.Shift) => func(0, 1)
  }
  def keyDataDigitFunction[B](func: Int => B): PartialFunction[KeyData, B] = _ match {
    case KeyTypedData(c) if '0' <= c && c <= '9' => func(c - '0')
  }
}
