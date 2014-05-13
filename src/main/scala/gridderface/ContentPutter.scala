package gridderface

trait ContentPutter {
  def putCell(cpos: CellPosition, rcont: RectContent): Unit
  def putEdge(epos: EdgePosition, lcont: LineContent): Unit
  def putIntersection(ipos: IntersectionPosition, pcont: PointContent): Unit
}
