package gridderface

import java.awt.Graphics2D

class GriddableAdaptor[A <: Griddable](private var _griddable: A) extends Griddable {
  def griddable = _griddable
  listenTo(_griddable)
  
  reactions += {
    case GriddableChanged(g) if g == _griddable => publish(GriddableChanged(this))
  }
  
  
  def griddable_=(other: A) = {
    deafTo(_griddable)
    _griddable = other
    listenTo(_griddable)
    publish(GriddableChanged(this))
  }
  
  def grid(prov: GridProvider, g2d: Graphics2D): Unit = {
    griddable.grid(prov, g2d)
  }

}