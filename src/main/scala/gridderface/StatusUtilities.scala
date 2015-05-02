package gridderface

import java.io.IOException
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.RenderedImage
import java.awt.image.BufferedImage
import java.awt.Paint
import java.awt.Graphics2D
import scala.collection.GenSeqLike

object StatusUtilities {
  def tryToFloat(str: String): Status[Float] = {
    try {
      Success(str.toFloat)
    } catch {
      case e: NumberFormatException => Failed("Error: cannot parse float: " + str)
    }
  }
  def tryToDouble(str: String): Status[Double] = {
    try {
      Success(str.toDouble)
    } catch {
      case e: NumberFormatException => Failed("Error: cannot parse double: " + str)
    }
  }
  def tryToInt(str: String): Status[Int] = {
    try {
      Success(str.toInt)
    } catch {
      case e: NumberFormatException => Failed("Error: cannot parse int: " + str)
    }
  }
  def tryToBoolean(str: String): Status[Boolean] = str match {
    case "0" => Success(false)
    case "1" => Success(true )
    case _ => Failed("Error: cannot parse boolean: " + str)
  }
  def tryToInts(strs: Seq[String]): Status[Seq[Int]] = {
    (strs map tryToInt).foldLeft(Success(List.empty): Status[Seq[Int]])(
      (collected, next) =>
        for (c <- collected; n <- next) yield (c :+ n))
  }
  def countedIntArguments(strs: Seq[String], countIsAllowed: Int => Boolean): Status[Seq[Int]] = {
    for (
      _ <- counted(strs, countIsAllowed);
      result <- tryToInts(strs)
    ) yield result
  }
  val wrongArgumentNumberMessage = "Error: wrong number of arguments"
  // GenSeqLike[Any,Any] is largest type that contains .length
  // unfortunately, Array is not a GenSeqLike
  // it must be implicitly converted to WrappedArray, which is one
  // the <% bound ("can be viewed as") makes the compiler let us take Arrays
  // here, this generality isn't necessary but it's interesting Scala practice
  def counted[A <% GenSeqLike[Any,Any]](strs: A, countIsAllowed: Int => Boolean,
      leftMessage: String = wrongArgumentNumberMessage): Status[A] = {
    if (countIsAllowed(strs.length)) Success(strs) else Failed(leftMessage)
  }
  def getSingleElement[Elt,A <% GenSeqLike[Elt,Any]](args: A): Status[Elt] = {
    for (_ <- counted(args, (1 == _))) yield args(0)
  }
  def getOptionalElement[Elt,A <% GenSeqLike[Elt,Any]](args: A): Status[Option[Elt]] = {
    args.length match {
      case 0 => Success(None)
      case 1 => Success(Some(args(0)))
      case _ => Failed(wrongArgumentNumberMessage)
    }
  }
  def getTwoElements[Elt,A <% GenSeqLike[Elt,Any]](args: A): Status[(Elt, Elt)] = {
    for (_ <- counted(args, (2 == _))) yield (args(0), args(1))
  }
  def getElementByIndex[Elt,A <% GenSeqLike[Elt,Any]](args: A, ix: Int): Status[Elt] = {
    for (_ <- counted(args, (ix < _))) yield args(ix)
  }
  def writeImage(img: RenderedImage, filename: String): Status[String] = {
    try {
      val file = new File(filename)
      // Some weird ImageIO implementation weakness makes doing this naively screw up.
      // http://stackoverflow.com/questions/12074858/java-imageio-exception-weirdness
      if (!file.exists()){
        if (file.createNewFile()) {
          ImageIO.write(img, "png", file)
          Success("Created and written image to file " + filename)
        } else Failed("Error: cannot create file: " + filename)
      } else if (file.canWrite()){
        ImageIO.write(img, "png", file)
        Success("Written image to file " + filename)
      } else Failed("Error: cannot write to file: " + filename)
    } catch {
      case e: IOException => Failed("Error: IOException: " + e.getMessage())
    }
  }
  def readImage(filename: String): Status[BufferedImage] = {
    try {
      val file = new File(filename)
      if (file.exists() && file.canRead()){
        Success(ImageIO.read(file))
      } else {
        Failed("Error: file does not exist or is not readable: " + filename)
      }
    } catch {
      case e: IOException => Failed("Error: IOException: " + e.getMessage())
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
