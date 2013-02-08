package gridderface

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
  def decorationCommand(args: Array[String], dim: (Int, Int)) = {
    val (rows, cols) = dim
    for (
      _ <- CommandUtilities.counted(args, 0 <, "Error: decorate requires arguments").right;
      result <- (args(0) match {
        case "clear" => decorationClearCommand(args.tail)
        case "edge" => decorationEdgeCommand(args.tail, rows, cols)
        case "border" => decorationBorderCommand(args.tail, rows, cols)
        case sc => Left("Error: unrecognized decorate subcommand: " + sc)
      }).right
    ) yield result
  }
}