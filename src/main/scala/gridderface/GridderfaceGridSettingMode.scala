package gridderface

import scala.swing.event.MouseEvent

class GridderfaceGridSettingMode(prov: MutableGridProvider) extends GridderfaceMode {

  val name = "Grid"
  def moveGrid(xd: Int, yd: Int){
    prov.xOffset += (xd * gridMultiplier)
    prov.yOffset += (yd * gridMultiplier)
  }
  def adjustGridSize(d: Double) {
    prov.rowHeight = (prov.rowHeight + d) max 0.0
    prov.colWidth = (prov.colWidth + d) max 0.0
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
    "%.2fx%.2f (M2^%d%s)".format(prov.colWidth, prov.rowHeight, gridExponent,
        if (negateFlag) "`" else "")
  }
  listenTo(prov)
  reactions += {
    case GridChanged() => publish(StatusChanged(this))
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

}
