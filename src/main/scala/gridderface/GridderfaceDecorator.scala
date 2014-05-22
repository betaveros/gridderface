package gridderface

import scala.collection.immutable.HashMap
import java.awt.Color
import gridderface.stamp.Strokes
import gridderface.stamp.StrokeLineStamp
import gridderface.stamp.FixedMark

class GridderfaceDecorator(seq: GriddableAdaptor[GriddableSeq]) {
  def decorationClearCommand(restArgs: Array[String]): Status[String] = {
    for (
      _ <- CommandUtilities.counted(
        restArgs, (0 == _), "Error: decorate clear takes no extra arguments")
    ) yield {
      seq.griddable = GriddableSeq.empty
      "Cleared decoration grids"
    }
  }
  def getBoundTuple(args: Array[String], defRows: Int, defCols: Int): Status[(Int, Int, Int, Int)] = {
    for (ints <- CommandUtilities.countedIntArguments(args, Set(0, 2, 4).contains(_))) yield {
      ints.length match {
        case 0 => (0, 0, defRows, defCols)
        case 2 => (0, 0, ints(0), ints(1))
        case 4 => (ints(0), ints(1), ints(2), ints(3))
      }
    }
  }
  def decorationEdgeCommand(restArgs: Array[String], rows: Int, cols: Int): Status[String] = {
    for (
      ecs <- CommandUtilities.getElementByIndex(restArgs, 0);
      bt <- getBoundTuple(restArgs.tail, rows, cols);
      econt <- GridderfaceStringParser.parseLineContentString(ecs)
    ) yield {
      seq.griddable = seq.griddable :+
        new HomogeneousEdgeGrid(econt, bt._1, bt._2, bt._3, bt._4)
      "Added decoration edge grid"
    }
  }
  def decorationBorderCommand(restArgs: Array[String], rows: Int, cols: Int): Status[String] = {
    for (
      ecs <- CommandUtilities.getElementByIndex(restArgs, 0);
      bt <- getBoundTuple(restArgs.tail, rows, cols);
      econt <- GridderfaceStringParser.parseLineContentString(ecs)
    ) yield {
      seq.griddable = seq.griddable :+
        new HomogeneousBorderGrid(econt, bt._1, bt._2, bt._3, bt._4)
      "Added decoration edge grid"
    }
  }
  abstract class PresetGriddable {
    def createGriddable(rowStart: Int, colStart: Int, rowEnd: Int, colEnd: Int): Griddable
  }
  case class PresetBorder(cont: LineContent) extends PresetGriddable {
    def createGriddable(rowStart: Int, colStart: Int, rowEnd: Int, colEnd: Int) = {
      new HomogeneousBorderGrid(cont, rowStart, colStart, rowEnd, colEnd)
    }
  }
  case class PresetEdges(cont: LineContent) extends PresetGriddable {
    def createGriddable(rowStart: Int, colStart: Int, rowEnd: Int, colEnd: Int) = {
      new HomogeneousEdgeGrid(cont, rowStart, colStart, rowEnd, colEnd)
    }
  }
  case class PresetIntersections(cont: PointContent) extends PresetGriddable {
    def createGriddable(rowStart: Int, colStart: Int, rowEnd: Int, colEnd: Int) = {
      new HomogeneousIntersectionGrid(cont, rowStart, colStart, rowEnd, colEnd)
    }
  }
  private val plainPreset = new PresetEdges(new LineStampContent(Strokes.thinStamp, Color.BLACK))
  private val boldPreset = new PresetBorder(new LineStampContent(Strokes.normalStamp, Color.BLACK))
  private val dashedPreset = new PresetEdges(new LineStampContent(Strokes.thinDashedStamp, Color.GRAY))
  private val slitherPresetList = List(
    new PresetEdges(new LineStampContent(new StrokeLineStamp(0.1875f), new Color(254, 254, 254))),
    new PresetIntersections(new PointStampContent(FixedMark.createDiskStamp(0.125), Color.BLACK)))
  private val whitedashedPresetList = List(
    new PresetEdges(new LineStampContent(Strokes.thinStamp, new Color(254, 254, 254))),
    dashedPreset)

  val presetMap: Map[String, List[PresetGriddable]] = HashMap(
    "plain" -> List(plainPreset),
    "dashed" -> List(dashedPreset),
    "bold" -> List(boldPreset),
    "slither" -> slitherPresetList,
    "slitherlink" -> slitherPresetList,
    "fillomino" -> (whitedashedPresetList
      :+ new PresetBorder(new LineStampContent(Strokes.normalStamp, Color.BLACK))),
    "whitedashed" -> whitedashedPresetList,
    "corral" -> whitedashedPresetList,
    "nurikabe" -> List(plainPreset, boldPreset)
  )
  def getPresetAsStatus(presetName: String) = {
    presetMap get presetName match {
      case Some(list) => Success(list)
      case None => Failed("Error: no such preset: " + presetName)
    }
  }
  def decorationPresetCommand(restArgs: Array[String], rows: Int, cols: Int, append: Boolean): Status[String] = {
    val presetTokens = (for (s <- CommandUtilities.getElementByIndex(restArgs, 0)) yield s split ',')

    val presetStatList = presetTokens map (_ map getPresetAsStatus)
    val presetListStat = for (psl <- presetStatList; res <- psl.foldLeft(
      Success(List.empty): Status[List[PresetGriddable]])(
            (list, preset) => for (li <- list; p <- preset) yield (p ++ li)
            // note: each preset list is to be applied left-to-right
            // but multiple preset args are applied right-to-left
            // blah, I find it more intuitive that way
      )) yield res
    // only do the decorating if we're certain all presets exist
    for (plist <- presetListStat; bd <- getBoundTuple(restArgs.tail, rows, cols)) yield {
      if (append) seq.griddable = seq.griddable ++ (plist map (_.createGriddable(bd._1, bd._2, bd._3, bd._4)))
      else seq.griddable = new GriddableSeq(plist map (_.createGriddable(bd._1, bd._2, bd._3, bd._4)))
      ""
    }
  }
  def decorationCommand(args: Array[String], dim: (Int, Int)): Status[String] = {
    val (rows, cols) = dim
    for (
      _ <- CommandUtilities.counted(args, (0 < _), "Error: decorate requires arguments");
      result <- (args(0) match {
        case "clear" => decorationClearCommand(args.tail)
        case "edge" => decorationEdgeCommand(args.tail, rows, cols)
        case "border" => decorationBorderCommand(args.tail, rows, cols)
        case "pre" => decorationPresetCommand(args.tail, rows, cols, false)
        case "p" => decorationPresetCommand(args.tail, rows, cols, false)
        case "padd" => decorationPresetCommand(args.tail, rows, cols, true)
        case sc => Failed("Error: unrecognized decorate subcommand: " + sc)
      })
    ) yield result
  }
}
