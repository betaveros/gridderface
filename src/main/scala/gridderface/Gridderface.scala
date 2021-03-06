package gridderface

import swing._
import scala.swing.event._
import java.awt.Color
import java.awt.Paint
import gridderface.stamp._
import scala.collection.immutable.HashMap
import scala.collection.mutable.ListBuffer
import javax.swing.TransferHandler
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.geom._
import javax.swing.KeyStroke
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.IOException
import java.io.File
import scala.io.Source

object Gridderface extends SimpleSwingApplication {

  val selectedManager = new SelectedPositionManager(Some(new IntersectionPosition(0, 0)))
  val underGridModel = new GriddableModel()
  val gridModel = new GriddableModel()
  // qq, initialization order
  val gridMode = new GridderfaceGridSettingMode(gridModel, 0, SimpleGrid.defaultGrid, 10, 10,
    (pt: Point2D) => gridPanel.viewToGrid(pt))
  val bg = new GriddableImageHolder(None)

  val edgeGridHolder = new GriddableAdaptor(HomogeneousEdgeGrid.defaultEdgeGrid(10, 10))

  val decorationGridSeq = new GriddableAdaptor[GriddableSeq](new GriddableSeq(List.empty))
  val decorator = new GridderfaceDecorator(decorationGridSeq)
  var generationDimensions: Option[(Int, Int)] = None // (rows, cols)

  def computePosition(pt: Point) = {
    val gridpt = gridPanel.viewToGrid(pt)
    gridMode.grid.computePosition(gridpt.getX(), gridpt.getY())
  }
  private def ctrl(c: Char): Char = (c - 64).toChar
  def setImage(img: Image): Unit = {
    bg.image = Some(img); gridPanel.repaint()
  }
  val globalKeyListReactions: PartialFunction[List[KeyData], KeyResult] = {
    case List(KeyTypedData(':'            )) => commandLine.startCommandMode(':'); KeyComplete
    case List(KeyTypedData('@'            )) => commandLine.startCommandMode('@'); KeyComplete
    case List(KeyTypedData('\u0004' /*^D*/)) => setMode(drawMode); KeyComplete
    case List(KeyTypedData('\u0007' /*^G*/)) => setMode(gridMode); KeyComplete
    case List(KeyTypedData('\u0010' /*^P*/)) => setMode(viewportMode); KeyComplete
    case List(KeyTypedData('\u0016' /*^V*/)) => TransferHandler.getPasteAction().actionPerformed(new java.awt.event.ActionEvent(gridPanel.peer, java.awt.event.ActionEvent.ACTION_PERFORMED, "paste")); KeyComplete
  }

  private def currentMouseReactions = currentMode.mouseReactions
  def setMode(mode: GridderfaceMode) {
    currentMode = mode
    selectedManager.paint = mode.cursorPaint
    modeLabel.text = mode.name
    statusLabel.text = mode.status
  }

  private def withOpacity(g: Griddable, prov: GridProvider, alpha: Float, name: String): OpacityBuffer = {
    new OpacityBuffer(g, prov, alpha)
  }

  private var keyList: ListBuffer[KeyData] = ListBuffer()
  private def isUsefulKeyData(d: KeyData) = d match {
    case KeyTypedData('\t') => false // cannot detect modifiers, apparently
    case KeyTypedData(_) => true
    case KeyPressedData(Key.Tab,   _) => true
    case KeyPressedData(Key.Left,  _) => true
    case KeyPressedData(Key.Down,  _) => true
    case KeyPressedData(Key.Up,    _) => true
    case KeyPressedData(Key.Right, _) => true
    case _ => false
  }

  private def processKeyEvent(e: KeyEvent) {
    val dat = KeyData extract e
    if (isUsefulKeyData(dat)) {
      keyList += dat
      val res: KeyResult = (currentMode.keyListReactions
        // orElse (currentMode.commandPrefixMap andThen commandLine.startCommandMode)
        orElse globalKeyListReactions).applyOrElse(keyList.toList, Function.const(KeyUndefined)_)
      res match {
        case KeyComplete => keyList.clear()
        case KeyCompleteWith(stat) => {
          keyList.clear()
          commandLine.showStatus(stat)
        }
        case KeyIncomplete => // nothing
        case KeyUndefined => {
          commandLine showError "Undefined key sequence " ++ {
            (keyList map {_.toKeyString}).mkString
          }
          keyList.clear()
        }
      }
    }
  }

  val griddableList: List[Tuple5[Griddable, GridProvider, Float, String, Boolean]] = List(
    (bg, gridMode, 1.0f, "image", true),
    (underGridModel, gridMode, 1.0f, "undercontent", true),
    (decorationGridSeq, gridMode, 1.0f, "decoration", true),
    (gridModel, gridMode, 1.0f, "content", true),
    (gridMode, gridMode.currentProvider, 0.5f, "grid", false),
    (selectedManager, gridMode.currentProvider, 0.75f, "cursor", false))
  val opacityBufferList: List[Tuple3[String, OpacityBuffer, Boolean]] =
    for ((g, prov, opacity, name, generateFlag) <- griddableList) yield {
      val obuf = withOpacity(g, prov, opacity, name)
      (name, obuf, generateFlag)
    }
  val generatingOpacityBufferList: List[OpacityBuffer] =
    for ((_, buf, flag) <- opacityBufferList; if flag) yield buf
  val opacityBufferMap = HashMap(
    (for ((name, buf, _) <- opacityBufferList) yield (name, buf)): _*)

  val gridPanel: GridPanel = new GridPanel {
    peer setFocusTraversalKeysEnabled false // prevent tab key from being consumed
    listenTo(keys)
    listenTo(mouse.clicks)
    listenTo(mouse.moves)

    for ((_, obuf, _) <- opacityBufferList) { buffers += obuf; listenTo(obuf) }

    listenTo(gridMode)
    reactions += {
      // "lift" turns PartialFunctions into total functions returning Option[B]
      // here we just use it to silence uncaught events
      case event: KeyEvent => processKeyEvent(event)
      case event: MouseEvent => currentMouseReactions lift event
      case event: BufferChanged => repaint()
      case event: GridChanged => repaint()
    }
    peer.setTransferHandler(new ImageTransferHandler(setImage))
    //val pasteKey = "paste"
    //peer.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), pasteKey)
    //peer.getActionMap().put(pasteKey, TransferHandler.getPasteAction())
    focusable = true
    requestFocus
  }
  def getOpacityBufferAsStatus(name: String): Status[OpacityBuffer] = {
    opacityBufferMap get name match {
      case None => Failed("no such buffer: " + name)
      case Some(buf) => Success(buf)
    }
  }
  def safeAlpha(a: Float): Status[Float] = {
    if (0 <= a && a <= 1) Success(a)
    else Failed("alpha out of range: " + a)
  }
  def fixedOpacityCommand(op: Float, args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.opacity = op; "" }
  }
  def opacityCommand(args: Array[String]): Status[String] = {
    for (
      _ <- StatusUtilities.counted(args, (2 == _));
      buf <- getOpacityBufferAsStatus(args(0));
      a <- StatusUtilities.tryToFloat(args(1));
      a2 <- safeAlpha(a)
    ) yield { buf.opacity = a2; "" }
  }
  def setBlendModeCommand(mode: OpacityBuffer.BlendMode.Value, args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.blendMode = mode; "" }
  }
  def setAntiAliasCommand(aa: Boolean, args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.antiAlias = aa; "" }
  }
  def setTextAntiAliasCommand(aa: Boolean, args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.textAntiAlias = aa; "" }
  }
  def getDimensionPair(args: Array[String], defaultVal: Int): Status[(Int, Int)] = {
    for (ints <- StatusUtilities.countedIntArguments(args, Set(0, 1, 2).contains(_))) yield {
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

      gridMode.grid = SimpleGrid.generationGrid
      bg.image = Some(StatusUtilities.createFilledImage(
        32 * (1 + cols), 32 * (1 + rows), Color.WHITE))
      gridMode.setRowColCount(rows, cols)
      "Ready for generation"
    }
  }
  def generateImage(): Option[BufferedImage] = {
    bg.image map (baseImg => {
      val w = baseImg.getWidth(null)
      val h = baseImg.getHeight(null)
      val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
      val g = img.createGraphics()
      for (buf <- generatingOpacityBufferList) {
        buf.draw(g, new AffineTransform(), new Dimension(w, h))
      }
      img
    })
  }
  def writeGeneratedImage(args: Array[String]): Status[String] = {
    if (args.length != 1) Failed("wrong number of arguments")
    else generateImage() match {
      case None => Failed("no background")
      case Some(img) => StatusUtilities.writeImage(img, args(0))

    }
  }
  def readImageFrom(args: Array[String]): Status[String] = {
    for (arg <- StatusUtilities.getSingleElement(args);
         result <- StatusUtilities.readImage(arg)) yield {
      bg.image = result; "OK"
    }
  }
  def readImageFromScreen() = {
    bg.image = new java.awt.Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()))
  }
  def setColor(arg: String) = {
    for (set <- PaintStringifier.parsePaintSet(arg)) yield {
      drawMode.setPaintSet(set); "Set color to " + arg
    }
  }
  def readColorCommand(args: Array[String]) = {
    StatusUtilities.getSingleElement(args) flatMap setColor
  }
  def decorationCommand(args: Array[String]): Status[String] = {
    generationDimensions match {
      case Some(dim) => decorator.decorationCommand(args, dim)
      case None => Failed("not in generation mode")
    }
  }
  def copyToClipboard(str: String): Status[String] = {
    val sel = new StringSelection(str)
    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(sel, null)
    Success("Copied " + str.length + " characters to clipboard")
  }
  def dumpGriddables(args: Array[String]): Status[String] = {
    for (arg <- StatusUtilities.getOptionalElement(args)) yield arg match {
      case None => gridModel flatMap (GridderfaceStringifier stringifyGriddablePositionMap _.griddableMap) foreach (println _)
      case Some(f) => {
        val p = new java.io.PrintWriter(new java.io.File(f))
        try {
          gridModel flatMap (GridderfaceStringifier stringifyGriddablePositionMap _.griddableMap) foreach (p println _)
        } finally {
          p.close()
        }
      }
    }
    Success("Dumped")
  }
  def parseGriddablesFrom(args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      _ <- GridderfaceStringifier.readGriddablePositionsFromInto(
        Source.fromFile(arg), gridModel)
    ) yield "OK"
  }

  def handleColonCommand(str: String): Status[String] = {
    val parts = "\\s+".r.split(str.trim)
    if (parts.length > 0) {
      parts(0) match {
        case "hello" => Success("Hello world!")

        // just pretend this is for testing if errors work
        case "Ni!" => Failed("Do you demand a shrubbery?")
        case "quit" => sys.exit()
        case "clearimage" =>
          bg.image = None; Success("Image cleared")
        case "resetgrid" => {
          gridMode.grid = SimpleGrid.defaultGrid
          Success("Grid reset")
        }
        case "write" => writeGeneratedImage(parts.tail)
        case "read" => readImageFrom(parts.tail)

        case "pwd" => Success(new File(".").getAbsolutePath)

        case "init"    => initGeneration(parts.tail)
        case "initgen" => initGeneration(parts.tail)

        case "hide" => fixedOpacityCommand(0f, parts.tail)
        case "show" => fixedOpacityCommand(1f, parts.tail)

        case "opacity" => opacityCommand(parts.tail)
        case "op"      => opacityCommand(parts.tail)

        case "multiply" => setBlendModeCommand(OpacityBuffer.Multiply, parts.tail)
        case "mul"      => setBlendModeCommand(OpacityBuffer.Multiply, parts.tail)
        case "min"      => setBlendModeCommand(OpacityBuffer.Min, parts.tail)
        case "normal"   => setBlendModeCommand(OpacityBuffer.Normal, parts.tail)
        case "antialias" => setAntiAliasCommand(true, parts.tail)
        case "aa"        => setAntiAliasCommand(true, parts.tail)
        case "noantialias" => setAntiAliasCommand(false, parts.tail)
        case "noaa"        => setAntiAliasCommand(false, parts.tail)
        case "textantialias" => setTextAntiAliasCommand(true, parts.tail)
        case "taa"           => setTextAntiAliasCommand(true, parts.tail)
        case "notextantialias" => setTextAntiAliasCommand(false, parts.tail)
        case "notaa"           => setTextAntiAliasCommand(false, parts.tail)

        case "color" => readColorCommand(parts.tail)

        case "decorate" => decorationCommand(parts.tail)
        case "dec"      => decorationCommand(parts.tail)

        case "screen" => readImageFromScreen(); Success("Read image from screen")

        case "dump" => dumpGriddables(parts.tail)
        case "parse" => parseGriddablesFrom(parts.tail)
        case "guess" => bg.image match {
          case Some(img: BufferedImage) => gridMode.grid = GridGuesser guess img; Success("Guess")
          case Some(_) => Failed("background image not buffered (!?)")
          case None => Failed("no background image")
        }

        case "pngpaste" => {
          ImageTransferHack.getImage() map (img => {
            setImage(img); "Read image from pngpaste -"
          })
        }

        case command => currentMode.handleColonCommand(command, parts.tail)
      }
    } else Success("")
  }
  val commandLine = new CommandLinePanel((char, str) => char match {
    case ':' => handleColonCommand(str)
    case '@' => copyToClipboard(str)
    case _ => currentMode.handleCommand(char, str)
  })
  val modeLabel = new Label()
  val statusLabel = new Label()
  val drawMode = new GridderfaceDrawingMode("Draw",
    selectedManager,
    List(gridModel, underGridModel),
    List(None, Some("under")),
    0,
    pt => computePosition(pt), commandLine.startCommandMode(_))
  lazy val viewportMode = new GridderfaceViewportMode(gridPanel)
  var currentMode: GridderfaceMode = drawMode
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
