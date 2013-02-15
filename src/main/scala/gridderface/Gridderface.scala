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
  val prov = new MutableGridProvider(32.0, 32.0, 0.0, 0.0)
  // the default arguments work, but Eclipse randomly bugs me about it meh

  def createEdgeGrid(rows: Int, cols: Int) = new HomogeneousEdgeGrid(
    new LineStampContent(Strokes.normalDashedStamp, Color.BLACK), rows, cols)
  val edgeGridHolder = new GriddableAdaptor(createEdgeGrid(10, 10))

  val decorationGridSeq = new GriddableAdaptor[GriddableSeq](new GriddableSeq(List.empty))
  val decorator = new GridderfaceDecorator(decorationGridSeq)
  var generationDimensions: Option[(Int, Int)] = None // (rows, cols)

  def computePosition(pt: Point) = {
    val gridpt = gridPanel.viewToGrid(pt)
    prov.computePosition(gridpt.getX(), gridpt.getY())
  }
  val globalReactions: PartialFunction[KeyData, Unit] = {
    case KeyTypedData(':') => commandLine.startCommandMode(':')
    case KeyPressedData(Key.D, Key.Modifier.Control) => setMode(drawMode)
    case KeyPressedData(Key.G, Key.Modifier.Control) => setMode(gridMode)
    case KeyPressedData(Key.P, Key.Modifier.Control) => setMode(viewportMode)
    // Note: Control-V is mapped in java.swing with InputMaps & co.
    // since I can't seem to simulate pasting nicely.
  }
  var currentMode: GridderfaceMode = drawMode
  

  private def currentKeyReactions = (currentMode.keyReactions
    orElse (currentMode.commandPrefixMap andThen commandLine.startCommandMode)
    orElse globalReactions)
  private def currentMouseReactions = currentMode.mouseReactions
  def setMode(mode: GridderfaceMode) {
    currentMode = mode
    modeLabel.text = mode.name
    statusLabel.text = mode.status

  }

  private def withOpacity(g: Griddable, alpha: Float, name: String): OpacityBuffer = {
    new OpacityBuffer(g, alpha)
  }

  val griddableList: List[Tuple3[Griddable, Float, String]] = List(
    (bg, 1.0f, "image"),
    (decorationGridSeq, 1.0f, "decoration"),
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

    for ((_, obuf) <- opacityBufferList) { buffers += obuf; listenTo(obuf) }

    listenTo(prov)
    reactions += {
      // "lift" turns PartialFunctions into total functions returning Option[B]
      // here we just use it to silence uncaught events
      case event: KeyEvent => currentKeyReactions lift (KeyData extract event)
      case event: MouseEvent => currentMouseReactions lift event
      case event: BufferChanged => repaint()
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
  def getOpacityBufferAsStatus(name: String): Status[OpacityBuffer] = {
    opacityBufferMap get name match {
      case None => Failed("Error: no such buffer: " + name)
      case Some(buf) => Success(buf)
    }
  }
  def safeAlpha(a: Float): Status[Float] = {
    if (0 <= a && a <= 1) Success(a)
    else Failed("Error: alpha out of range: " + a)
  }
  def fixedOpacityCommand(op: Float, args: Array[String]): Status[String] = {
    for (
      arg <- CommandUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.opacity = op; "" }
  }
  def opacityCommand(args: Array[String]): Status[String] = {
    for (
      _ <- CommandUtilities.counted(args, (2 == _));
      buf <- getOpacityBufferAsStatus(args(0));
      a <- CommandUtilities.tryToFloat(args(1));
      a2 <- safeAlpha(a)
    ) yield { buf.opacity = a2; "" }
  }
  def getDimensionPair(args: Array[String], defaultVal: Int): Status[(Int, Int)] = {
    for (ints <- CommandUtilities.countedIntArguments(args, Set(0, 1, 2).contains(_))) yield {
      ints.length match {
        case 0 => (defaultVal, defaultVal)
        case 1 =>
          val v = ints(0); (v, v)
        case 2 => (ints(0), ints(1))
      }
    }
  }

  def initGeneration(args: Array[String]): Status[String] = {
    for (rctup <- getDimensionPair(args, 10)) yield {
      val (rows, cols) = rctup

      generationDimensions = Some((rows, cols))
      
      decorationGridSeq.griddable = GriddableSeq.empty

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
      decorationGridSeq.grid(prov, g)
      ggrid.grid(prov, g)
      img
    })
  }
  def writeGeneratedImage(args: Array[String]): Status[String] = {
    if (args.length != 1) Failed("Error: wrong number of arguments")
    else generateImage() match {
      case None => Failed("Error: no background")
      case Some(img) => CommandUtilities.writeImage(img, args(0))

    }
  }
  def readImageFrom(args: Array[String]): Status[String] = {
    for (arg <- CommandUtilities.getSingleElement(args);
         result <- CommandUtilities.readImage(arg)) yield {
      bg.image = result; "OK"
    }
  }
  def setColor(arg: String) = {
    for (set <- GridderfaceStringParser.parseColorString(arg)) yield {
      drawMode.setPaintSet(set); "Set color to " + arg
    }
  }
  def readColorCommand(args: Array[String]) = {
    CommandUtilities.getSingleElement(args) flatMap setColor
  }
  def parseHomogeneousGrid(pair: (String, String), rows: Int, cols: Int) = {
    val (choice, str) = pair
    choice match {
      case "edge" => GridderfaceStringParser.parseLineContentString(str) map {
        new HomogeneousEdgeGrid(_, rows, cols)
      }
    }
  }
  def decorationCommand(args: Array[String]): Status[String] = {
    generationDimensions match {
      case Some(dim) => decorator.decorationCommand(args, dim)
      case None => Failed("Error: not in generation mode")
    }
  }
  def handleColonCommand(str: String): Status[String] = {
    val parts = "\\s+".r.split(str.trim)
    if (parts.length > 0) {
      parts(0) match {
        case "hello" => Success("Hello world!")

        // just pretend this is for testing if errors work
        case "Ni!" => Failed("Do you demand a shrubbery?")
        case "quit" => sys.exit()
        case "clear" =>
          ggrid.clear(); Success("Content cleared")
        case "clearimage" =>
          bg.image = None; Success("Image cleared")
        case "resetgrid" => {
          prov.xOffset = 0
          prov.yOffset = 0
          prov.rowHeight = 32
          prov.colWidth = 32
          Success("Grid reset")
        }
        case "write" => writeGeneratedImage(parts.tail)
        case "read" => readImageFrom(parts.tail)
        case "init" => initGeneration(parts.tail)
        case "initgen" => initGeneration(parts.tail)
        case "hide" => fixedOpacityCommand(0f, parts.tail)
        case "show" => fixedOpacityCommand(1f, parts.tail)
        case "opacity" => opacityCommand(parts.tail)
        case "op" => opacityCommand(parts.tail)
        case "color" => readColorCommand(parts.tail)
        case "decorate" => decorationCommand(parts.tail)
        case "dec" => decorationCommand(parts.tail)
        case "lock" => drawMode.lockedToCells = true; Success("Locked")
        case "unlock" => drawMode.lockedToCells = false; Success("Unlocked")
        
        case _ => Failed("Unrecognized command")
      }
    } else Success("")
  }
  val commandLine = new CommandLinePanel((char, str) => char match {
    case ':' => handleColonCommand(str)
    case _ => currentMode.handleCommand(char, str)
  })
  val modeLabel = new Label()
  val statusLabel = new Label()
  val drawMode = new GridderfaceDrawingMode(selectedManager, ggrid, pt => computePosition(pt))
  val gridMode = new GridderfaceGridSettingMode(prov)
  lazy val viewportMode = new GridderfaceViewportMode(gridPanel)
  // gah, the initialization sequence here is tricky
  
  listenTo(drawMode, gridMode, viewportMode)
  setMode(drawMode)

  
  reactions += {
    case StatusChanged(src: GridderfaceMode) => {
      statusLabel.text = currentMode.status
    }
  }
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

