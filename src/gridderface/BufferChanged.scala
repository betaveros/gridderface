package gridderface

import scala.swing.event.Event

case class BufferChanged(source: OpacityBuffer) extends Event {}