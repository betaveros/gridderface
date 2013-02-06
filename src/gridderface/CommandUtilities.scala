package gridderface

import java.io.IOException
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.RenderedImage
import java.awt.image.BufferedImage
import java.awt.Paint
import java.awt.Graphics2D
import scala.collection.GenSeqLike

object CommandUtilities {
  def tryToFloat(str: String): Either[String, Float] = {
    // There's a cool Try object in Scala 2.10 that lets you do this more monadically.
    try {
      Right(str.toFloat)
    } catch {
      case e: NumberFormatException => Left("Error: cannot parse float: " + str)
    }
  }
  def tryToInt(str: String): Either[String, Int] = {
    try {
      Right(str.toInt)
    } catch {
      case e: NumberFormatException => Left("Error: cannot parse int: " + str)
    }
  }
  def tryToInts(strs: Seq[String]): Either[String, Seq[Int]] = {
    (strs map tryToInt).foldLeft(Right(List.empty): Either[String, Seq[Int]])(
      (collected, next) =>
        for (c <- collected.right; n <- next.right) yield (c :+ n))
  }
  def countedIntArguments(strs: Seq[String], countIsAllowed: Int => Boolean): Either[String, Seq[Int]] = {
    for (
      _ <- counted(strs, countIsAllowed).right;
      result <- tryToInts(strs).right
    ) yield result
  }
  val wrongArgumentNumberMessage = "Error: wrong number of arguments"
  // GenSeqLike[Any,Any] is largest type that contains .length
  // unfortunately, Array is not a GenSeqLike
  // it must be implicitly converted to WrappedArray, which is one
  // the <% bound ("can be viewed as") makes the compiler let us take Arrays
  // here, this generality isn't necessary but it's interesting Scala practice
  def counted[A <% GenSeqLike[Any,Any]](strs: A, countIsAllowed: Int => Boolean, 
      leftMessage: String = wrongArgumentNumberMessage): Either[String, A] = {
    if (countIsAllowed(strs.length)) Right(strs) else Left(leftMessage)
  }
  def getSingleElement[Elt,A <% GenSeqLike[Elt,Any]](strs: A): Either[String, Elt] = {
    for (_ <- counted(strs, 1 ==).right) yield strs(0)
  }
  def writeImage(img: RenderedImage, filename: String) = {
    try {
      ImageIO.write(img, "png", new File(filename));
      Right("Written image to " + filename)
    } catch {
      case e: IOException => Left("Error: IOException: " + e.getMessage())
    }
  }
  def createFilledImage(w: Int, h: Int, p: Paint) = {
    val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g = img.getGraphics().asInstanceOf[Graphics2D]
    g.setPaint(p)
    g.fillRect(0, 0, w, h)
    img
  }

}