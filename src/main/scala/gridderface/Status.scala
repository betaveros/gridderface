package gridderface

sealed abstract class Status[+A] {
  self =>
  def map[Y](f: (A) => Y): Status[Y]
  def flatMap[Y](f: (A) => Status[Y]): Status[Y]
  def filter(f: (A) => Boolean): Status[A]
  // We really shouldn't define filter but there's this place in
  // GridderfaceDrawingMode where we want to pattern-match it into a tuple
  // and because Scala's compiler is currently silly that is not considered
  // irrefutable. Meh.
  def foreach[U](f: A => U): Unit
  def withFilter(p: A => Boolean): WithFilter = new WithFilter(p)
  // I copied this from Option.scala
  class WithFilter(p: A => Boolean) {
    def map[B](f: A => B): Status[B] = self filter p map f
    def flatMap[B](f: A => Status[B]): Status[B] = self filter p flatMap f
    def foreach[U](f: A => U): Unit = self filter p foreach f
    def withFilter(q: A => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
  }
}
case class Failed(message: String) extends Status[Nothing] {
  def map[Y](f: (Nothing) => Y) = this
  def flatMap[Y](f: (Nothing) => Status[Y]) = this
  def filter(f: (Nothing) => Boolean) = this
  def foreach[U](f: (Nothing) => U) = Unit
}
case class Success[+A](a: A) extends Status[A] {
  def map[Y](f: (A) => Y) = Success(f(a))
  def flatMap[Y](f: (A) => Status[Y]) = f(a)
  def filter(f: (A) => Boolean) = if (f(a)) this else Failed("filtered out")
  def foreach[U](f: (A) => U) = { f(a); Unit }
}
