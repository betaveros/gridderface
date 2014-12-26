package gridderface.stamp

import java.awt._

case class StrokeLineStamp(sv: StrokeVal) extends ScalableLineStamp {
  def drawUnit(g2d: Graphics2D): Unit = {
    g2d setStroke sv.stroke
    g2d draw unitLine
  }
}
