package gridderface

import gridderface.stamp._
import java.awt._
import scala.swing.event.KeyEvent
import scala.swing.event.KeyTyped
import scala.collection.immutable.HashMap

case class ContentSet(val name: String,
  val rectContent: Option[RectContent], val lineContent: Option[LineContent], val pointContent: Option[PointContent])

object ContentSet {
  def makeStampContentSet(name: String, p: Paint, s: StampSet): ContentSet = ContentSet(
    name,
    s.rectStamp map (st => RectStampContent(st, p)),
    s.lineStamp map (st => LineStampContent(st, p)),
    s.pointStamp map (st => PointStampContent(st, p))
  )
  def makeBlackContentSet(ss: StampSet): ContentSet = makeStampContentSet(ss.name, Color.BLACK, ss)
  def makeFillContentSet(ps: PaintSet): ContentSet = makeStampContentSet(ps.name, ps.paint, StampSet.fillSet)
  val fillMap = (
    StampSet.defaultMap.mapValues(s => makeBlackContentSet(s))
    ++
    PaintSet.basicMap.mapValues(s => makeFillContentSet(s))
  )
}
