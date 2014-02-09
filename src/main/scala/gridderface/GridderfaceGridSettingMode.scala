package gridderface

import scala.swing.event.MouseEvent

class GridderfaceGridSettingMode(private var _prov: SimpleGridProvider) extends GridderfaceMode with GridProvider {

  val name = "Grid"
  def computeX(col: Int) = _prov.computeX(col)
  def computeY(row: Int) = _prov.computeY(row)
  def computeRow(y: Double) = _prov.computeRow(y)
  def computeCol(x: Double) = _prov.computeCol(x)

  def moveGrid(xd: Int, yd: Int){
    _prov = _prov.offsetBy(xd * gridMultiplier, yd * gridMultiplier)
    publish(GridChanged())
  }
  def adjustGridSize(d: Double) {
    _prov = _prov.gridSizeAdjustedBy(d, d)
    publish(GridChanged())
  }
  def grid: SimpleGridProvider = _prov
  def grid_=(g: SimpleGridProvider): Unit = {
    if (_prov equals g) return
    _prov = g
    publish(GridChanged())
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
    "%.2fx%.2f (M2^%d%s)".format(_prov.colWidth, _prov.rowHeight, gridExponent,
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
    setGridMultiplierReactions orElse negateMultiplierReactions andThen {u: Unit => true})
  def handleCommand(prefix: Char, str: String) = Success("")
  val mouseReactions: PartialFunction[MouseEvent, Unit] = Map.empty

  override def simpleGrid = _prov
}
