import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *  ClipboardHandling.java
 *
 *  ClipboardHandling sets and gets image data to and from the clipboard.
 *
 *  NOTE: The code for handling the clipboard was found online, and is almost unaltered since it does exactly
 *        what it needs to do:
 *
 *          http://stackoverflow.com/questions/4552045/copy-bufferedimage-to-clipboard
 *          http://alvinalexander.com/java/java-clipboard-image-copy-paste
 *
 *  @author Jan Øyvind Kruse
 *  @version 1.0
 */

public class ClipboardHandling implements ClipboardOwner {

    public void copyText(String clipboardText) {
        StringSelection stringSelection = new StringSelection(clipboardText);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public BufferedImage getImage()
    {
        //  This sometimes causes an exception in IntelliJ ... Do not know how to fix it, but it
        //  seems harmless.

        //  Exception "java.lang.ClassNotFoundException: ... "

        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
        {
            try {
                return (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
            }
            catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.err.println("cH - getImage: Not image data");
        }
        return null;
    }

    public void lostOwnership(Clipboard clip, Transferable trans) {
        System.out.println("cH: Lost Clipboard Ownership");
    }

    //  ==============================================================================================================

    private class TransferableImage implements Transferable {

        Image i;

        public TransferableImage(Image i) {
            this.i = i;
        }


        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
                return i;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }


        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
            return flavors;
        }


        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();

            for (int i = 0; i < flavors.length; i++) {
                if (flavor.equals(flavors[i])) {
                    return true;
                }
            }

            return false;
        }
    }
}
