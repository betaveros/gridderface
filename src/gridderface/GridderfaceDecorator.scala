package gridderface

import scala.collection.immutable.HashMap
import java.awt.Color
import gridderface.stamp.Strokes
import gridderface.stamp.StrokeLineStamp
import gridderface.stamp.FixedMark

class GridderfaceDecorator(seq: GriddableAdaptor[GriddableSeq]) {
  def decorationClearCommand(restArgs: Array[String]) = {
    for (
      _ <- CommandUtilities.counted(
        restArgs, 0 ==, "Error: decorate clear takes no extra arguments").right
    ) yield {
      seq.griddable = List.empty
      "Cleared decoration grids"
    }
  }
  def decorationEdgeCommand(restArgs: Array[String], rows: Int, cols: Int) = {
    for (
      arg <- CommandUtilities.getSingleElement(restArgs).right;
      econt <- GridderfaceStringParser.parseLineContentString(arg).right
    ) yield {
      seq.griddable = seq.griddable :+
        new HomogeneousEdgeGrid(econt, rows, cols)
      "Added decoration edge grid"
    }
  }
  def decorationBorderCommand(restArgs: Array[String], rows: Int, cols: Int) = {
    for (
      arg <- CommandUtilities.getSingleElement(restArgs).right;
      econt <- GridderfaceStringParser.parseLineContentString(arg).right
    ) yield {
      seq.griddable = seq.griddable :+
        new HomogeneousBorderGrid(econt, rows, cols)
      "Added decoration edge grid"
    }
  }
  abstract class PresetGriddable {
    def createGriddable(rows: Int, cols: Int): Griddable
  }
  case class PresetBorder(cont: LineContent) extends PresetGriddable {
    def createGriddable(rows: Int, cols: Int) = {
      new HomogeneousBorderGrid(cont, rows, cols)
    }
  }
  case class PresetEdges(cont: LineContent) extends PresetGriddable {
    def createGriddable(rows: Int, cols: Int) = {
      new HomogeneousEdgeGrid(cont, rows, cols)
    }
  }
  case class PresetIntersections(cont: PointContent) extends PresetGriddable {
    def createGriddable(rows: Int, cols: Int) = {
      new HomogeneousIntersectionGrid(cont, rows, cols)
    }
  }
  val presetMap: Map[String, List[PresetGriddable]] = HashMap(
    "plain" -> List(new PresetEdges(new LineStampContent(Strokes.thinStamp, Color.BLACK))),
    "dashed" -> List(new PresetEdges(new LineStampContent(Strokes.thinDashedStamp, Color.GRAY))),
    "bold" -> List(new PresetBorder(new LineStampContent(Strokes.normalStamp, Color.BLACK))),
    "nwgrid" -> List(
        new PresetEdges(new LineStampContent(new StrokeLineStamp(0.1875f), new Color(254, 254, 254))),
        new PresetIntersections(new PointStampContent(FixedMark.createDiskStamp(0.125), Color.BLACK))),
    "whitedashed" -> List(
        new PresetEdges(new LineStampContent(Strokes.thinStamp, new Color(254, 254, 254))), 
        new PresetEdges(new LineStampContent(Strokes.thinDashedStamp, Color.GRAY)))
  )
  def getPresetAsEither(presetName: String) = {
    presetMap get presetName match {
      case Some(list) => Right(list)
      case None => Left("Error: no such preset: " + presetName)
    }
  }
  def decorationPresetCommand(restArgs: Array[String], rows: Int, cols: Int) = {
    val presetsEither = (restArgs map getPresetAsEither).foldLeft(
        Right(List.empty): Either[String, List[PresetGriddable]])(
            (list, preset) => for (li <- list.right; p <- preset.right) yield (p ++ li)
            // note: each preset list is to be applied left-to-right
            // but multiple preset args are applied right-to-left
            // blah, I find it more intuitive that way
        )
    // only do the decorating if we're certain all presets exist
    for (presets <- presetsEither.right) yield {
      seq.griddable = presets map (_.createGriddable(rows, cols)); ""
    }
  }
  def decorationCommand(args: Array[String], dim: (Int, Int)): Either[String, String] = {
    val (rows, cols) = dim
    for (
      _ <- CommandUtilities.counted(args, 0 <, "Error: decorate requires arguments").right;
      result <- (args(0) match {
        case "clear" => decorationClearCommand(args.tail)
        case "edge" => decorationEdgeCommand(args.tail, rows, cols)
        case "border" => decorationBorderCommand(args.tail, rows, cols)
        case "pre" => decorationPresetCommand(args.tail, rows, cols)
        case sc => Left("Error: unrecognized decorate subcommand: " + sc)
      }).right
    ) yield result
  }
}