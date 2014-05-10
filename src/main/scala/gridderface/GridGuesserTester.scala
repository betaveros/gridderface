package gridderface

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object GridGuesserTester {
  def main(args: Array[String]): Unit = {
    Option(ImageIO.read(new File("fillomino-no-path.png"))) match {
      case Some(img) => GridGuesser guess img
      case None => println("Cannot read :(")
    }
  }
}

