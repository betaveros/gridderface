package gridderface

import javax.swing.TransferHandler
import javax.swing.TransferHandler.TransferSupport
import java.awt.datatransfer.DataFlavor
import java.awt.Image
import javax.imageio.ImageIO
import java.io.File

class ImageTransferHandler(response: Image => Unit) extends TransferHandler {
  override def canImport(ts: TransferSupport) =
    ts.isDataFlavorSupported(DataFlavor.imageFlavor) ||
    ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
  override def importData(ts: TransferSupport) = {
    if (!canImport(ts)) false
    else {
      val t = ts.getTransferable
      if (ts isDataFlavorSupported DataFlavor.imageFlavor){
        // Some[A] collect PartialFunction[A, B] => Some[B] if in domain, None else
        Some(t getTransferData DataFlavor.imageFlavor) collect {
          case img: Image => response(img)
        }
      } else {
        Some(t.getTransferData(DataFlavor.javaFileListFlavor)) collect {
          case flist: java.util.List[_] => {
            if (flist.size > 0) Some(flist.get(0)) collect {
              case f: File => {
                response(ImageIO.read(f))
              }
            }
          }
        }
      }
      true
    }
  }
}
