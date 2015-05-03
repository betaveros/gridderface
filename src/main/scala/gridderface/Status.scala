package gridderface

sealed abstract class Status[+A] {
  def map[Y](f: (A) => Y): Status[Y]
  def flatMap[Y](f: (A) => Status[Y]): Status[Y]
  def filter(f: (A) => Boolean): Status[A]
  // We really shouldn't define filter but there's this place in
  // GridderfaceDrawingMode where we want to pattern-match it into a tuple
  // and because Scala's compiler is currently silly that is not considered
  // irrefutable. Meh.
}
case class Failed(message: String) extends Status[Nothing] {
  def map[Y](f: (Nothing) => Y) = this
  def flatMap[Y](f: (Nothing) => Status[Y]) = this
  def filter(f: (Nothing) => Boolean) = this
}
case class Success[+A](a: A) extends Status[A] {
  def map[Y](f: (A) => Y) = Success(f(a))
  def flatMap[Y](f: (A) => Status[Y]) = f(a)
  def filter(f: (A) => Boolean) = if (f(a)) this else Failed("filtered out")
}
