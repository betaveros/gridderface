package gridderface

import scala.swing.event.MouseEvent

class GridderfaceGridSettingMode(private var _grid: SimpleGrid) extends GridderfaceMode with GridProvider {
  val name = "Grid"
  def grid: SimpleGrid = _grid
  def grid_=(g: SimpleGrid): Unit = {
    if (_grid equals g) return
    _grid = g
    publish(GridChanged())
    publish(StatusChanged(this))
  }
  def moveGrid(xd: Int, yd: Int){
    grid = grid.offsetBy(xd * gridMultiplier, yd * gridMultiplier)
  }
  def adjustGridSize(d: Double) {
    grid = grid.gridSizeAdjustedBy(d, d)
  }
  var gridExponent = 0
  def gridMultiplier = scala.math.pow(2.0, gridExponent)
  var negateFlag = false
  val moveGridReactions = KeyDataCombinations.keyDataXYFunction(moveGrid)
  val resizeGridReactions: PartialFunction[KeyData, Unit] = {
      case KeyTypedData('+') => adjustGridSize(gridMultiplier)
      case KeyTypedData('-') => adjustGridSize(-gridMultiplier)
  }
  def status = {
    "%.2fx%.2f (M2^%d%s)".format(_grid.colWidth, _grid.rowHeight, gridExponent,
        if (negateFlag) "`" else "")
  }
  val setGridMultiplierReactions = KeyDataCombinations.keyDataDigitFunction(dig => {
    gridExponent = if (negateFlag) -dig else dig
    negateFlag = false
    publish(StatusChanged(this))
  })
  val negateMultiplierReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData('`') => negateFlag = true; publish(StatusChanged(this))
  }
  val keyListReactions = new SingletonListPartialFunction(
    moveGridReactions orElse resizeGridReactions orElse
    setGridMultiplierReactions orElse negateMultiplierReactions andThen {u: Unit => KeyComplete})
  def handleCommand(prefix: Char, str: String) = Success("")
  val mouseReactions: PartialFunction[MouseEvent, Unit] = Map.empty
}
