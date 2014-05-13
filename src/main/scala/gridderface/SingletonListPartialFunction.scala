package gridderface

import scala.runtime.AbstractPartialFunction
class SingletonListPartialFunction[-A,+B](f: PartialFunction[A,B]) extends AbstractPartialFunction[List[A],B] {
  override def isDefinedAt(la: List[A]) = la.length == 1 && f.isDefinedAt(la.head)
  override def applyOrElse[A1 <: List[A], B1 >: B](x: A1, default: (A1) => B1): B1 = {
    if (x.length == 1 && f.isDefinedAt(x.head)) f(x.head)
    else default(x)
  }
}
