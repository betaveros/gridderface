package gridderface

sealed abstract class Status[+A] {
  def map[Y](f: (A) => Y): Status[Y] 
  def flatMap[Y](f: (A) => Status[Y]): Status[Y]
}
case class Failed(message: String) extends Status[Nothing] {
  def map[Y](f: (Nothing) => Y) = this
  def flatMap[Y](f: (Nothing) => Status[Y]) = this
}
case class Success[+A](a: A) extends Status[A] {
  def map[Y](f: (A) => Y) = Success(f(a))
  def flatMap[Y](f: (A) => Status[Y]) = f(a)
}