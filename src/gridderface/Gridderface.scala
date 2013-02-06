package gridderface

import swing._
import scala.swing.event._
import java.awt.Color
import java.awt.Paint
import gridderface.stamp._
import scala.collection.immutable.HashMap
import javax.swing.TransferHandler
import java.awt.Toolkit
import javax.swing.KeyStroke
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.IOException
import java.io.File

object Gridderface extends SimpleSwingApplication {

  val selectedManager = new SelectedPositionManager(Some(new IntersectionPosition(0, 0)))
  val ggrid = new GriddableGrid
  val bg = new GriddableImageHolder(None)
  val prov = new MutableGridProvider(32.0, 32.0)

  def createEdgeGrid(rows: Int, cols: Int) = new HomogeneousEdgeGrid(
      new LineStampContent(Strokes.normalDashedStamp, Color.BLACK), rows, cols)
  val edgeGridHolder = new GriddableAdaptor(createEdgeGrid(10, 10))
  
  var generationGridSeq = new GriddableAdaptor[GriddableSeq](new GriddableSeq(List.empty))
  var generationDimensions: Option[(Int, Int)] = None // (rows, cols)

  def computePosition(pt: Point) = prov.computePosition(pt.x, pt.y)
  val modeLabel = new Label()
  val statusLabel = new Label()
  val drawMode = new GridderfaceDrawingMode(selectedManager, ggrid, pt => computePosition(pt))
  val gridMode = new GridderfaceGridSettingMode(prov)
  val globalReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData(':') => commandLine.startCommandMode(':')
    case KeyPressedData(Key.D, Key.Modifier.Control) => setMode(drawMode)
    case KeyPressedData(Key.G, Key.Modifier.Control) => setMode(gridMode)
    // Note: Control-V is mapped in java.swing with InputMaps & co.
    // since I can't seem to simulate pasting nicely.
  }
  var currentMode: GridderfaceMode = drawMode
  listenTo(drawMode, gridMode)
  reactions += {
    case StatusChanged(src: GridderfaceMode) => {
      statusLabel.text = currentMode.status
    }
  }

  private def currentKeyReactions = (currentMode.keyReactions
    orElse (currentMode.commandPrefixMap andThen commandLine.startCommandMode)
    orElse globalReactions)
  private def currentMouseReactions = currentMode.mouseReactions
  def setMode(mode: GridderfaceMode) {
    currentMode = mode
    modeLabel.text = mode.name
    statusLabel.text = mode.status

  }

  setMode(drawMode)

  private def withOpacity(g: Griddable, alpha: Float, name: String): OpacityBufferGriddable = {
    new OpacityBufferGriddable(g, () => gridPanel.size, alpha)
  }

  val griddableList: List[Tuple3[Griddable, Float, String]] = List(
    (bg, 1.0f, "image"),
    (generationGridSeq, 1.0f, "gengrid"),
    (ggrid, 1.0f, "content"),
    (edgeGridHolder, 0.5f, "grid"),
    (selectedManager, 0.75f, "cursor"))
  val opacityBufferList = for ((g, opacity, name) <- griddableList) yield {
    val obuf = withOpacity(g, opacity, name)
    (name, obuf)
  }
  val opacityBufferMap = HashMap(opacityBufferList: _*)

  val gridPanel = new GridPanel(prov) {
    listenTo(keys)
    listenTo(mouse.clicks)

    for ((_, obuf) <- opacityBufferList) { griddables += obuf; listenTo(obuf) }

    listenTo(prov)
    reactions += {
      // "lift" turns PartialFunctions into total functions returning Option[B]
      // here we just use it to silence uncaught events
      case event: KeyEvent => currentKeyReactions lift (KeyData extract event)
      case event: MouseEvent => currentMouseReactions lift event
      case event: GriddableChanged => repaint()
      case event: GridChanged => repaint()
    }
    // looks like datatransfer will have to fully fall back to java.swing
    peer.setTransferHandler(new ImageTransferHandler(
      { img => bg.image = Some(img); repaint }))
    val pasteKey = "paste"
    peer.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), pasteKey)
    peer.getActionMap().put(pasteKey, TransferHandler.getPasteAction())
    focusable = true
    requestFocus
  }
  def getOpacityBufferAsEither(name: String): Either[String, OpacityBufferGriddable] = {
    opacityBufferMap get name match {
      case None => Left("Error: no such buffer: " + name)
      case Some(buf) => Right(buf)
    }
  }
  def safeAlpha(a: Float): Either[String, Float] = {
    if (0 <= a && a <= 1) Right(a)
    else Left("Error: alpha out of range: " + a)
  }
  def fixedOpacityCommand(op: Float, args: Array[String]): Either[String, String] = {
    for (
      arg <- CommandUtilities.getSingleElement(args).right;
      buf <- getOpacityBufferAsEither(arg).right
    ) yield { buf.opacity = op; "" }
  }
  def opacityCommand(args: Array[String]): Either[String, String] = {
    for (
      _ <- CommandUtilities.counted(args, 2 ==).right;
      buf <- getOpacityBufferAsEither(args(0)).right;
      a <- CommandUtilities.tryToFloat(args(1)).right;
      a2 <- safeAlpha(a).right
    ) yield { buf.opacity = a2; "" }
  }
  def getDimensionPair(args: Array[String], defaultVal: Int): Either[String, (Int, Int)] = {
    for (ints <- CommandUtilities.countedIntArguments(args, Set(0, 1, 2).contains(_)).right) yield {
      ints.length match {
        case 0 => (defaultVal, defaultVal)
        case 1 =>
          val v = ints(0); (v, v)
        case 2 => (ints(0), ints(1))
      }
    }
  }

  def initGeneration(args: Array[String]) = {
    for (rctup <- getDimensionPair(args, 10).right) yield {
      val (rows, cols) = rctup

      generationDimensions = Some((rows, cols))
      
      prov.xOffset = 16
      prov.yOffset = 16
      prov.rowHeight = 32
      prov.colWidth = 32
      bg.image = Some(CommandUtilities.createFilledImage(
        32 * (1 + cols), 32 * (1 + rows), Color.WHITE))
      edgeGridHolder.griddable = createEdgeGrid(rows, cols)
      "Ready for generation"
    }
  }
  def generateImage(): Option[BufferedImage] = {
    bg.image map (baseImg => {
      val w = baseImg.getWidth(null)
      val h = baseImg.getHeight(null)
      val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
      val g = img.createGraphics()
      g.drawImage(baseImg, 0, 0, w, h, null)
      ggrid.grid(prov, g)
      img
    })
  }
  def writeGeneratedImage(args: Array[String]) = {
    if (args.length != 1) Left("Error: wrong number of arguments")
    else generateImage() match {
      case None => Left("Error: no background")
      case Some(img) => CommandUtilities.writeImage(img, args(0))

    }

  }
  def handleColonCommand(str: String): Either[String, String] = {
    val parts = "\\s+".r.split(str.trim)
    if (parts.length > 0) {
      parts(0) match {
        case "hello" => Right("Hello world!")

        // just pretend this is for testing if errors work
        case "Ni!" => Left("Do you demand a shrubbery?")
        case "quit" => sys.exit()
        case "clear" =>
          ggrid.clear(); Right("Content cleared")
        case "clearimage" =>
          bg.image = None; Right("Image cleared")
        case "resetgrid" => {
          prov.xOffset = 0
          prov.yOffset = 0
          prov.rowHeight = 32
          prov.colWidth = 32
          Right("Grid reset")
        }
        case "write" => writeGeneratedImage(parts.tail)
        case "initgen" => initGeneration(parts.tail)
        case "hide" => fixedOpacityCommand(0f, parts.tail)
        case "show" => fixedOpacityCommand(1f, parts.tail)
        case "opacity" => opacityCommand(parts.tail)
        case "op" => opacityCommand(parts.tail)
        case "plain" => generationDimensions match {
          case None => Left("Error: not in generation mode")
          case Some((rows, cols)) => {
            generationGridSeq.griddable =
              generationGridSeq.griddable :+ new HomogeneousEdgeGrid(
                new LineStampContent(Strokes.normalStamp, Color.BLACK), rows, cols);
            Right("Plain grid added")
          }
        }
        case _ => Left("Unrecognized command")
      }
    } else Right("")
  }
  val commandLine = new CommandLinePanel((char, str) => char match {
    case ':' => handleColonCommand(str)
    case _ => currentMode.handleCommand(char, str)
  })

  def top = new MainFrame {
    title = "t3h Gridderface 2.0"
    contents = new BorderPanel {
      layout(gridPanel) = BorderPanel.Position.Center
      layout(new BoxPanel(Orientation.Horizontal) {
        contents += Swing.HStrut(10)
        contents += modeLabel
        contents += Swing.HStrut(10)
        contents += statusLabel
        contents += Swing.HStrut(10)
        contents += commandLine
      }) = BorderPanel.Position.South
    }
    size = new java.awt.Dimension(800, 600)
  }

}

