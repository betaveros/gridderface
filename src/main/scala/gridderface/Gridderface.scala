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
import java.awt.geom.AffineTransform
import javax.swing.KeyStroke
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.IOException
import java.io.File
import scala.io.Source

object Gridderface extends SimpleSwingApplication {

  val selectedManager = new SelectedPositionManager(Some(new IntersectionPosition(0, 0)))
  val gridList = new GriddablePositionMapList()
  // qq, initialization order
  val gridMode = new GridderfaceGridSettingMode(SimpleGrid.defaultGrid)
  val bg = new GriddableImageHolder(None)

  def createEdgeGrid(rs: Int, cs: Int, rc: Int, cc: Int) = new HomogeneousEdgeGrid(
    new LineStampContent(StrokeLineStamp(NormalDashedStrokeVal), Color.BLACK), rs, cs, rc, cc)
  val edgeGridHolder = new GriddableAdaptor(createEdgeGrid(0, 0, 10, 10))

  val decorationGridSeq = new GriddableAdaptor[GriddableSeq](new GriddableSeq(List.empty))
  val decorator = new GridderfaceDecorator(decorationGridSeq)
  var generationDimensions: Option[(Int, Int)] = None // (rows, cols)

  def computePosition(pt: Point) = {
    val gridpt = gridPanel.viewToGrid(pt)
    gridMode.grid.computePosition(gridpt.getX(), gridpt.getY())
  }
  private def ctrl(c: Char): Char = (c - 64).toChar
  val globalKeyListReactions: PartialFunction[List[KeyData], KeyResult] = {
    case List(KeyTypedData(':'         )) => commandLine.startCommandMode(':'); KeyComplete
    case List(KeyTypedData('@'         )) => commandLine.startCommandMode('@'); KeyComplete
    case List(KeyTypedData('\04' /*^D*/)) => setMode(drawMode); KeyComplete
    case List(KeyTypedData('\07' /*^G*/)) => setMode(gridMode); KeyComplete
    case List(KeyTypedData('\20' /*^P*/)) => setMode(viewportMode); KeyComplete
    case List(KeyTypedData('\26' /*^V*/)) => TransferHandler.getPasteAction().actionPerformed(new java.awt.event.ActionEvent(gridPanel.peer, java.awt.event.ActionEvent.ACTION_PERFORMED, "paste")); KeyComplete
    case List(KeyPressedData(Key.Tab, 0)) => {
      gridList.selectNextGrid()
      KeyCompleteWith(Success(gridList.status))
    }
    case List(KeyPressedData(Key.Tab, Key.Modifier.Shift)) => {
      gridList.selectNextGrid()
      KeyCompleteWith(Success(gridList.status))
    }
  }
  var currentMode: GridderfaceMode = drawMode

  private def currentMouseReactions = currentMode.mouseReactions
  def setMode(mode: GridderfaceMode) {
    currentMode = mode
    modeLabel.text = mode.name
    statusLabel.text = mode.status
  }

  private def withOpacity(g: Griddable, alpha: Float, name: String): OpacityBuffer = {
    new OpacityBuffer(g, alpha)
  }

  private var keyList: ListBuffer[KeyData] = ListBuffer()
  private def isUsefulKeyData(d: KeyData) = d match {
    case KeyTypedData('\t') => false // cannot detect modifiers, apparently
    case KeyTypedData(_) => true
    case KeyPressedData(Key.Tab, _) => true
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

  val griddableList: List[Tuple4[Griddable, Float, String, Boolean]] = List(
    (bg, 1.0f, "image", true),
    (decorationGridSeq, 1.0f, "decoration", true),
    (gridList, 1.0f, "content", true),
    (edgeGridHolder, 0.5f, "grid", false),
    (selectedManager, 0.75f, "cursor", false))
  val opacityBufferList: List[Tuple3[String, OpacityBuffer, Boolean]] =
    for ((g, opacity, name, generateFlag) <- griddableList) yield {
      val obuf = withOpacity(g, opacity, name)
      (name, obuf, generateFlag)
    }
  val generatingOpacityBufferList: List[OpacityBuffer] =
    for ((_, buf, flag) <- opacityBufferList; if flag) yield buf
  val opacityBufferMap = HashMap(
    (for ((name, buf, _) <- opacityBufferList) yield (name, buf)): _*)

  val gridPanel = new GridPanel(gridMode) {
    peer setFocusTraversalKeysEnabled false // prevent tab key from being consumed
    listenTo(keys)
    listenTo(mouse.clicks)

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
    peer.setTransferHandler(new ImageTransferHandler(
      { img => bg.image = Some(img); repaint }))
    //val pasteKey = "paste"
    //peer.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), pasteKey)
    //peer.getActionMap().put(pasteKey, TransferHandler.getPasteAction())
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
  def setMultiplyCommand(mul: Boolean, args: Array[String]): Status[String] = {
    for (
      arg <- StatusUtilities.getSingleElement(args);
      buf <- getOpacityBufferAsStatus(arg)
    ) yield { buf.multiply = mul; "" }
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
      edgeGridHolder.griddable = createEdgeGrid(0, 0, rows, cols)
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
        buf.drawOnGrid(gridMode.grid, g, new AffineTransform(), new Dimension(w, h))
      }
      img
    })
  }
  def writeGeneratedImage(args: Array[String]): Status[String] = {
    if (args.length != 1) Failed("Error: wrong number of arguments")
    else generateImage() match {
      case None => Failed("Error: no background")
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
    for (set <- GridderfaceStringParser.parseColorString(arg)) yield {
      drawMode.setPaintSet(set); "Set color to " + arg
    }
  }
  def readColorCommand(args: Array[String]) = {
    StatusUtilities.getSingleElement(args) flatMap setColor
  }
  /*
  def parseHomogeneousGrid(pair: (String, String), rows: Int, cols: Int) = {
    val (choice, str) = pair
    choice match {
      case "edge" => GridderfaceStringParser.parseLineContentString(str) map {
        new HomogeneousEdgeGrid(_, rows, cols)
      }
    }
  }
  */
  def decorationCommand(args: Array[String]): Status[String] = {
    generationDimensions match {
      case Some(dim) => decorator.decorationCommand(args, dim)
      case None => Failed("Error: not in generation mode")
    }
  }
  def copyToClipboard(str: String): Status[String] = {
    val sel = new StringSelection(str)
    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(sel, null)
    Success("Copied " + str.length + " characters to clipboard")
  }
  def dumpGriddables(args: Array[String]): Status[String] = {
    for (arg <- StatusUtilities.getOptionalElement(args)) yield arg match {
      case None => gridList flatMap (GridderfaceStringifier stringifyGriddablePositionMap _) foreach (println _)
      case Some(f) => {
        val p = new java.io.PrintWriter(new java.io.File(f))
        try {
          gridList flatMap (GridderfaceStringifier stringifyGriddablePositionMap _) foreach (p println _)
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
        Source.fromFile(arg), gridList)
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
        case "newgrid" =>
          gridList.addGrid(); Success(gridList.status ++ " New grid added")
        case "delgrid" =>
          gridList.removeGrid(); Success(gridList.status ++ " Current grid removed")
        case "delall" =>
          gridList.removeAll(); Success(gridList.status ++ " All grids removed")
        case "clear" =>
          gridList.clearGrid(); Success(gridList.status ++ " Content cleared")
        case "clearall" =>
          gridList.clearAll(); Success(gridList.status ++ " All content cleared")
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

        case "multiply" => setMultiplyCommand(true, parts.tail)
        case "mul"      => setMultiplyCommand(true, parts.tail)
        case "nomultiply" => setMultiplyCommand(false, parts.tail)
        case "nomul"      => setMultiplyCommand(false, parts.tail)

        case "color" => readColorCommand(parts.tail)

        case "decorate" => decorationCommand(parts.tail)
        case "dec"      => decorationCommand(parts.tail)

        case "lock"   => drawMode.lockToCells(); Success("Locked to cells")
        case "ilock"  => drawMode.lockToIntersections(); Success("Locked to intersections")
        case "unlock" => drawMode.unlock(); Success("Unlocked")

        case "screen" => readImageFromScreen(); Success("Read image from screen")

        case "dump" => dumpGriddables(parts.tail)
        case "parse" => parseGriddablesFrom(parts.tail)
        case "guess" => bg.image match {
          case Some(img: BufferedImage) => gridMode.grid = GridGuesser guess img; Success("Guess")
          case Some(_) => Failed("Background image not buffered (!?)")
          case None => Failed("No background image")
        }

        case _ => Failed("Unrecognized command")
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
  val drawMode = new GridderfaceDrawingMode(selectedManager, gridList,
    pt => computePosition(pt), commandLine.startCommandMode(_))
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
