import com.formdev.flatlaf.FlatDarculaLaf;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;

/*
 * TODO  - Implement a Palette class that lets the user pick from a variety of different palettes
 *  to suit their intended target -- this is important so the dithering algorithm picks the appropriate colors
 *  candidates: ANSI (KoBra MUD), Git Bash, mIRC
 *  * Try out an encoding for mIRC scripts
 *
 *  TODO - 2021 03 09 - When saving as a text file, divide into three sections:
 *      a) The color matrix of the primary color (as it is now)
 *      b) The color matrix of the secondary color
 *      c) The ascii
 *      This way it will be possible to massage that data if there is a custom need not covered by saving as (.AA, .SH, .MRC).
 *      If encoding is set to 'Custom', then copy should copy the text file, and when saving the (.AA, .SH, .MRC)
 *      option should be disabled.
 */

public class ANSI_generator extends JFrame {

    FileHandling fH;
    ClipboardHandling cH;
    C3 c3;
    Palette pal;

    C3[] sixteenPalette;
    C3[] eightPalette;

    String HTML_before;
    String HTML;
    String HTML_after;

    String saveHTML;

    String fileName = "Untitled";
    static String appName = "KoBra MUD ANSI Art Generator";
    static String versionNumber = "1.0";

    static int WIDTH = 79;
    static int MAX_HEIGHT = 86;  // 100

    static double asciiAmount = 6.0;

    static int[][][] sixteenMap = new int[MAX_HEIGHT][WIDTH][2];  // Used when saving .mud or .aa files
    static int[][][] eightMap = new int[MAX_HEIGHT][WIDTH][2];

    static double[][][] sixteenDist = new double[MAX_HEIGHT][WIDTH][2];  // Map with closest and next nearest distance
    static double[][][] eightDist = new double[MAX_HEIGHT][WIDTH][2];

    // Difference between the distance of the primary and secondary colors
    static double[][] sixteenFactor = new double[MAX_HEIGHT][WIDTH];
    static double[][] eightFactor = new double[MAX_HEIGHT][WIDTH];

    // Arrays for the ASCII characters
    static String[][] sixteenCharacterMap = new String[MAX_HEIGHT][WIDTH];
    static String[][] eightCharacterMap = new String[MAX_HEIGHT][WIDTH];

    String ASCII_CHARS = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";

    JMenuBar menuBar;
    JMenu fileMenu, encodingMenu, editMenu, imageMenu, helpMenu, subtractMenu;
    JMenuItem openMenuItem, saveMenuItem, undoMenuItem, copyMenuItem, pasteMenuItem;
    JMenuItem levelsMenuItem, blurMenuItem, sharpenMenuItem;
    JMenuItem brightenMenuItem, darkenMenuItem, monochromeMenuItem, invertMenuItem;
    JMenuItem aboutMenuItem;

    JCheckBoxMenuItem mudMenuItem, shellMenuItem, mircMenuItem;
    JCheckBoxMenuItem subtractAMenuItem, subtractBMenuItem, subtractCMenuItem, subtractDMenuItem, subtractEMenuItem;

    JButton undoButton, openButton, saveButton;
    JButton levelsButton, sharpenButton, monochromeButton;

    JSlider ditherSlider, asciiSlider;
    static JLabel firstLabel;
    JLabel secondLabel;
    JLabel thirdLabel;
    BufferedImage rawImage, sixteenImage, eightImage, undoImage;

    BufferedImage backupRawImage, backupUndoImage;  // Used when entering the levels dialog

    JTextPane textPane;

    JLabel statusLabel;

    public static Levels lev;

    public static int updatingMap;  // Which map we are updating
    public static int X;
    public static int Y;         // Global variables to keep track when updating the maps

    public static int subtract;

    // =============================================================================================

    public ANSI_generator(String name) {

        super(name);

        fH = new FileHandling();
        cH = new ClipboardHandling();
        c3 = new C3(0, 0, 0);
        pal = new Palette();


        fH.addParent( this );  // tell FileHandling about the ANSI generator
        c3.addParent( this);
        pal.addParent(this);
    }

    public void addComponentsToPane(final Container pane) throws IOException {

        int sliderWidth = 112;

        BorderLayout borderLayout = new BorderLayout(5, 0);
        pane.setLayout(borderLayout);

        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        URL url = ANSI_generator.class.getResource("images/undo_sm.png");
        ImageIcon icon = new ImageIcon(url);

        undoButton = new JButton(icon);

        openButton = new JButton("Open");
        saveButton = new JButton("Save");

        openButton.setPreferredSize(new Dimension(64,25));
        saveButton.setPreferredSize(new Dimension(64,25));

        sharpenButton = new JButton("Sharpen");
        monochromeButton = new JButton("Monochrome");

        levelsButton = new JButton("RGB Levels");

        buttonPanel.add(undoButton);
        buttonPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);

        buttonPanel.add(sharpenButton);

        buttonPanel.add(monochromeButton);
        buttonPanel.add(levelsButton);

        buttonPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);

        //Left to right component orientation is selected by default
        buttonPanel.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT);

        undoButton.addActionListener(e -> {
            try { undoAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        openButton.addActionListener(e -> {
            try { openAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        saveButton.addActionListener(e -> saveAction());

        sharpenButton.addActionListener(e -> {
            try { actionEffect(0);  /* 0 = sharpen */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        monochromeButton.addActionListener(e -> {
            try { actionEffect(4); } // 4 = monochrome
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        levelsButton.addActionListener(e -> { levelsAction(); });

        // =========================================================================================

        asciiSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        asciiSlider.addChangeListener(e -> asciiAction());

        asciiSlider.setPreferredSize(new Dimension(sliderWidth, 35));  // 120, 35
        buttonPanel.add(asciiSlider);

        asciiSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("ASCII amount"),
                BorderFactory.createEmptyBorder(10,0,10,0)));

        // =========================================================================================

        ditherSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        ditherSlider.addChangeListener(e -> ditherAction());

        ditherSlider.setPreferredSize(new Dimension(sliderWidth, 35));  // 120, 35
        buttonPanel.add(ditherSlider);

        ditherSlider.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Dither amount"),
                BorderFactory.createEmptyBorder(10,0,10,0)));

        // =========================================================================================

        pane.add(buttonPanel, BorderLayout.NORTH);

        // =========================================================================================

        // Create menu bar
        menuBar = new JMenuBar();

        // Build first menu
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // Group of JMenuItems
        openMenuItem = new JMenuItem("Open…");
        saveMenuItem = new JMenuItem("Save…");

        encodingMenu = new JMenu("Encoding");


        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();

        fileMenu.add(encodingMenu);

        mudMenuItem = new JCheckBoxMenuItem("KoBra MUD");
        shellMenuItem = new JCheckBoxMenuItem("UNIX Shell");
        mircMenuItem = new JCheckBoxMenuItem("mIRC");

        encodingMenu.add(mudMenuItem);
        encodingMenu.add(shellMenuItem);
        encodingMenu.add(mircMenuItem);

        // -------------------------------------------------------------------------------------------------------------

        mudMenuItem.setSelected(true);
        sixteenPalette = pal.getPalette(0, true);  // Type 0, full palette
        eightPalette = pal.getPalette(0, false);   // Type 0, half palette

        //mircMenuItem.setSelected(true);
        //sixteenPalette = pal.getPalette(2, true);  // Type 0, full palette
        //eightPalette = pal.getPalette(2, false);   // Type 0, half palette

        // -------------------------------------------------------------------------------------------------------------

        // Build second menu
        editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        // Group of JMenuItems
        undoMenuItem = new JMenuItem("Undo");

        copyMenuItem = new JMenuItem("Copy");

        pasteMenuItem = new JMenuItem("Paste");
        editMenu.add(undoMenuItem);
        editMenu.addSeparator();
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);

        // Third menu
        imageMenu = new JMenu("Image");
        menuBar.add(imageMenu);
        // Group of JMenuItems

        subtractMenu = new JMenu("Color subtraction");

        levelsMenuItem = new JMenuItem("RGB Levels…");
        blurMenuItem = new JMenuItem("Blur");
        sharpenMenuItem = new JMenuItem("Sharpen");
        brightenMenuItem = new JMenuItem("Brighten");
        darkenMenuItem = new JMenuItem("Darken");
        invertMenuItem = new JMenuItem("Invert");
        monochromeMenuItem = new JMenuItem("Monochrome");

        imageMenu.add(subtractMenu);

        // -------------------------------------------------------------------------------------------------------------

        subtractAMenuItem = new JCheckBoxMenuItem("20%");
        subtractBMenuItem = new JCheckBoxMenuItem("40%");
        subtractCMenuItem = new JCheckBoxMenuItem("60%");
        subtractDMenuItem = new JCheckBoxMenuItem("80%");
        subtractEMenuItem = new JCheckBoxMenuItem("100%");

        subtractMenu.add(subtractAMenuItem);
        subtractMenu.add(subtractBMenuItem);
        subtractMenu.add(subtractCMenuItem);
        subtractMenu.add(subtractDMenuItem);
        subtractMenu.add(subtractEMenuItem);

        subtractCMenuItem.setSelected(true);
        subtract = 2;

        // -------------------------------------------------------------------------------------------------------------

        imageMenu.addSeparator();

        imageMenu.add(levelsMenuItem);
        imageMenu.addSeparator();
        imageMenu.add(blurMenuItem);
        imageMenu.add(sharpenMenuItem);
        imageMenu.addSeparator();
        imageMenu.add(brightenMenuItem);
        imageMenu.add(darkenMenuItem);
        imageMenu.addSeparator();
        imageMenu.add(invertMenuItem);
        imageMenu.addSeparator();
        imageMenu.add(monochromeMenuItem);

        // Fourth menu
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        // Group of JMenuItems
        aboutMenuItem = new JMenuItem("About…");
        helpMenu.add(aboutMenuItem);

        openMenuItem.addActionListener(e -> {
            try { openAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        saveMenuItem.addActionListener(e -> saveAction());

        mudMenuItem.addActionListener(e -> { encodingAction(0); } );
        shellMenuItem.addActionListener(e -> { encodingAction(1); } );
        mircMenuItem.addActionListener(e -> { encodingAction(2); } );

        undoMenuItem.addActionListener(e -> {
            try { undoAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        copyMenuItem.addActionListener(e -> {
            try { copyAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        pasteMenuItem.addActionListener(e -> {
            try { pasteAction(); }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        subtractAMenuItem.addActionListener(e -> { subtractAction(0); } );
        subtractBMenuItem.addActionListener(e -> { subtractAction(1); } );
        subtractCMenuItem.addActionListener(e -> { subtractAction(2); } );
        subtractDMenuItem.addActionListener(e -> { subtractAction(3); } );
        subtractEMenuItem.addActionListener(e -> { subtractAction(4); } );

        levelsMenuItem.addActionListener(e -> { levelsAction(); });

        blurMenuItem.addActionListener(e -> {
            try { actionEffect(1);  /* 1 = blur */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        sharpenMenuItem.addActionListener(e -> {
            try { actionEffect(0);  /* 0 = sharpen */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        brightenMenuItem.addActionListener(e -> {
            try { actionEffect(2);  /* 2 = brighten */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        darkenMenuItem.addActionListener(e -> {
            try { actionEffect(3);  /* 3 = darken */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        invertMenuItem.addActionListener(e -> {
            try { actionEffect(5);  /* 5 = invert */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        monochromeMenuItem.addActionListener(e -> {
            try { actionEffect(4);  /* 4 = monochrome */ }
            catch (IOException ioException) { ioException.printStackTrace(); }
        });

        aboutMenuItem.addActionListener(e -> { aboutAction(); });

        KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlC = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlV = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlL = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlI = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        KeyStroke ctrlM = KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        openMenuItem.setAccelerator(ctrlO);
        saveMenuItem.setAccelerator(ctrlS);
        undoMenuItem.setAccelerator(ctrlZ);
        copyMenuItem.setAccelerator(ctrlC);
        pasteMenuItem.setAccelerator(ctrlV);
        levelsMenuItem.setAccelerator(ctrlL);
        invertMenuItem.setAccelerator(ctrlI);
        monochromeMenuItem.setAccelerator(ctrlM);

        this.setJMenuBar(menuBar);

        // =========================================================================================

        textPane = new JTextPane();
        textPane.setBackground(Color.black);

        HTMLEditorKit kit = new HTMLEditorKit();
        textPane.setEditorKit(kit);
        textPane.setDocument(kit.createDefaultDocument());

        textPane.setContentType("text/html");

        HTML_before = "<html>\n<head>\n<style>span {font-family: Monospace; font-size: 12;}</style>\n<title>KaaG ANSI Art</title>\n</head>\n" +
                "<body style=\"background-color:black;\">\n";
        HTML_after = "</body>\n</html>\n";

        textPane.setEditable(false);
        textPane.setHighlighter(null);
        textPane.setFocusable(false);

        // Put the editor pane in a scroll pane
        JScrollPane editorScrollPane = new JScrollPane(textPane);

        editorScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(589, 775));  // 600, 775
        editorScrollPane.setMinimumSize(new Dimension(589, 400));  // 600, 400

        editorScrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

        textPane.setCaretPosition(0);

        editorScrollPane.setBorder(new EmptyBorder(0,10,0,3));

        pane.add(editorScrollPane, BorderLayout.WEST);

        // =========================================================================================

        // We need to define this before updating the images, since doing that will update its text
        statusLabel = new JLabel("");

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.PAGE_AXIS));

        firstLabel = new JLabel();
        secondLabel = new JLabel();
        thirdLabel = new JLabel();

        loadImage(null, true);

        imagePanel.add(firstLabel);
        imagePanel.add(secondLabel);
        imagePanel.add(thirdLabel);

        firstLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Original"),
                BorderFactory.createEmptyBorder(10,10,10,10)));

        secondLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("16 colors"),
                BorderFactory.createEmptyBorder(10,10,10,10)));

        thirdLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("8 colors"),
                BorderFactory.createEmptyBorder(10,10,10,10)));

        // Put the editor pane in a scroll pane
        JScrollPane imageScrollPane = new JScrollPane(imagePanel);

        imageScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        imageScrollPane.setPreferredSize(new Dimension(582, 775));  // 600, 775
        imageScrollPane.setMinimumSize(new Dimension(582, 400));  // 600, 400
        imageScrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
        imageScrollPane.setBorder(new EmptyBorder(0,3,0,3));

        pane.add(imageScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();

        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(statusLabel);

        pane.add(bottomPanel, BorderLayout.SOUTH);
    }

    // =========================================================================================

    public void setStatusLabel() {

        String text, spacing = "      ";
        String encoding, palette;

        //String[] levels = { "20" };

        if(mudMenuItem.isSelected()) {
            encoding = "KoBra MUD";
            palette = "ANSI";
        }
        else if(shellMenuItem.isSelected()) {
            encoding = "UNIX Shell";
            palette = "Git Bash";
        }
        else {
            encoding = "mIRC";
            palette = "mIRC";
        }

        text = encoding + spacing +
                "Palette: " + palette + spacing +
                "Dimensions: " + rawImage.getWidth() + " pixels x " + rawImage.getHeight() + " pixels" + spacing +
                "Subtraction: " + (20 + subtract * 20) + "%" + spacing +
                "ASCII Amount: " + asciiSlider.getValue() + "%" + spacing +
                "Dither Amount: " + ditherSlider.getValue() + "%" + spacing;

        statusLabel.setText(text);
    }

    // =========================================================================================

    public void openAction() throws IOException {

        // Ask FileHandling to open a JFileChooser so the user can select a file to open
        BufferedImage new_image = fH.openFile();

        if(new_image != null) {
            loadImage(new_image, true);
        }

        firstLabel.requestFocus();  // To keep button from appearing pressed
    }

    // =========================================================================================

    public void saveAction() {
        fileName = fH.saveFile(fileName, rawImage.getHeight());
        firstLabel.requestFocus();  // To keep button from appearing pressed
        if(fileName == null) fileName = "Untitled";
    }

    // =========================================================================================

    public void encodingAction(int type) {

        // Type 0 = KoBra MUD, Type 1 = UNIX Shell, Type 2 = mIRC

        mudMenuItem.setSelected(false);
        shellMenuItem.setSelected(false);
        mircMenuItem.setSelected(false);

        switch(type) {
            case 0 :
                mudMenuItem.setSelected(true);
                sixteenPalette = pal.getPalette(0, true);  // full palette
                eightPalette = pal.getPalette(0, false);   // half palette
                break;
            case 1 :
                shellMenuItem.setSelected(true);
                sixteenPalette = pal.getPalette(1, true);  // full palette
                eightPalette = pal.getPalette(1, false);   // half palette
                break;
            case 2 :
                mircMenuItem.setSelected(true);
                sixteenPalette = pal.getPalette(2, true);  // full palette
                eightPalette = pal.getPalette(2, false);   // half palette
                break;
        }

        prepareImage(16, rawImage.getWidth(), rawImage.getHeight());
        prepareImage(8, rawImage.getWidth(), rawImage.getHeight());
    }

    // =========================================================================================

    public void undoAction() throws IOException {
        loadImage(undoImage, false);
        firstLabel.requestFocus();  // To keep button from appearing pressed
    }

    // =========================================================================================

    public void copyAction() throws IOException {
        System.out.println("@@ Copy action");

        String clipboardText = fH.getANSI(rawImage.getHeight(), 16);
        cH.copyText(clipboardText);
    }

    // =========================================================================================

    public void pasteAction() throws IOException {
        BufferedImage new_image = cH.getImage();  // Try to get image data from the clipboard

        if(new_image != null) {
            loadImage(new_image, true);
        }
    }
    // =========================================================================================

    public void subtractAction(int level) {

        subtractAMenuItem.setSelected(false);
        subtractBMenuItem.setSelected(false);
        subtractCMenuItem.setSelected(false);
        subtractDMenuItem.setSelected(false);
        subtractEMenuItem.setSelected(false);

        switch(level) {
            case 0 : subtractAMenuItem.setSelected(true); break;
            case 1 : subtractBMenuItem.setSelected(true); break;
            case 2 : subtractCMenuItem.setSelected(true); break;
            case 3 : subtractDMenuItem.setSelected(true); break;
            case 4 : subtractEMenuItem.setSelected(true); break;
        }

        subtract = level;

        prepareImage(16, rawImage.getWidth(), rawImage.getHeight());
        prepareImage(8, rawImage.getWidth(), rawImage.getHeight());
    }

    // =========================================================================================

    public void asciiAction() {
        asciiAmount = (asciiSlider.getValue() + 10.0) / 10.0;
        renderHTML();
    }

    // =========================================================================================

    public void ditherAction() {
        int new_width = rawImage.getWidth();
        int new_height = rawImage.getHeight();

        prepareImage(16, new_width, new_height);
        prepareImage(8, new_width, new_height);
    }

    // =========================================================================================

    public void aboutAction() {

        String msg = "<html><center>" + appName + " Version " + versionNumber + "<br>" +
                "Copyright &copy; 2021 Jan Øyvind Kruse<br><br>" +
                "Please read the included documentation for instructions.<br>" +
                "Send feedback to: jan.o.kruse@gmail.com</center>";

        JLabel msgLabel = new JLabel(msg, JLabel.CENTER);

        JOptionPane.showMessageDialog(this, msgLabel,
                "About " + appName, JOptionPane.PLAIN_MESSAGE);
    }

    // =========================================================================================

    // Used for storing rawImage, sixteenImage and eightImage

    public static BufferedImage toBufferedImage(Image img)
    {
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;  // Return the buffered image
    }

    // =========================================================================================

    public void loadImage(BufferedImage bi, boolean halve) throws IOException {

        ImageIcon imageIcon;
        Image newImg;
        float  w, h;
        int new_width, new_height;

        if(bi == null) {
            URL url = ANSI_generator.class.getResource("images/parrot.jpeg");
            bi = ImageIO.read(url);
        }

        w = bi.getWidth();
        h = bi.getHeight();

        if(halve) {

            // Scale the image data to the proper dimensions, executed when loading or pasting images

            new_width = WIDTH;
            new_height = Math.round(h * (WIDTH / w) / 2);

            // Check if the image exceeds MAX_HEIGHT here, if it does, it needs to be cropped

            if(new_height > MAX_HEIGHT) {

                // Appears to work as it should

                bi = bi.getSubimage(0, 0, (int)w, MAX_HEIGHT * 2 * (int)w / WIDTH);
                h = bi.getHeight();
                new_height = Math.round(h * (WIDTH / w) / 2);
            }

            // Scale it the smooth way
            newImg = bi.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_SMOOTH);

            rawImage = null;
        }
        else {

            // We do not want to rescale when performing image filters on an already existing image

            new_width = WIDTH;
            new_height = bi.getHeight();
            newImg = bi;
        }

        // Backup previous rawImage into undoImage

        if(rawImage != null) {
            undoImage = rawImage;
            undoMenuItem.setEnabled(true);
            undoButton.setEnabled(true);
        }
        else {
            undoImage = null;
            undoMenuItem.setEnabled(false);
            undoButton.setEnabled(false);
        }

        // Keep these for later
        rawImage = toBufferedImage(newImg);
        sixteenImage = toBufferedImage(newImg);
        eightImage = toBufferedImage(newImg);

        new_height = new_height * 2;

        // Double the vertical pixels simply for appearance, rawImage will be used for calculations

        newImg = newImg.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_REPLICATE);
        imageIcon = new ImageIcon(newImg);  // transform it back

        firstLabel.setIcon(imageIcon);
        firstLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        // The 16 color and 8 color versions should be put in these labels
        secondLabel.setIcon(imageIcon);
        secondLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        thirdLabel.setIcon(imageIcon);
        thirdLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        // ===========================================================================================================

        // Color quantization

        // For all the pixels in the raw image, check the RGB values of the pixel, then figure out
        // which color in the set palette (16 or 8 color) is closest

        new_width = rawImage.getWidth();
        new_height = rawImage.getHeight();

        prepareImage(16, new_width, new_height);
        prepareImage(8, new_width, new_height);

    }

    // ===============================================================================================================

    public void calculateTrueDistance(int x, int y, int amount) {

        C3 actual, primary, secondary;

        actual = new C3(rawImage.getRGB(x, y));

        if(amount == 16) {

            // We have to use the eight-color palette for the foreground colors,
            // cannot use the sixteen-color palette

            primary = sixteenPalette[ sixteenMap[y][x][0] ];
            secondary = eightPalette[ sixteenMap[y][x][1] ];

            sixteenDist[y][x][0] = primary.diff(actual);
            sixteenDist[y][x][1] = secondary.diff(actual);

            // Factor = secondary distance / primary distance
            sixteenFactor[y][x] = sixteenDist[y][x][1] / sixteenDist[y][x][0];

        } else {

            primary = eightPalette[ eightMap[y][x][0] ];
            secondary = eightPalette[ eightMap[y][x][1] ];

            eightDist[y][x][0] = primary.diff(actual);
            eightDist[y][x][1] = secondary.diff(actual);

            // Factor = secondary distance / primary distance
            eightFactor[y][x] = eightDist[y][x][1] / eightDist[y][x][0];
        }
    }

    // ===============================================================================================================

    public void prepareImage(int n, int new_width, int new_height) {

        double percentage = ditherSlider.getValue() / 100.0 ;  // 1.0
        C3[][] d = new C3[new_height][new_width];

        for (int y = 0; y < new_height; y++) {
            for (int x = 0; x < new_width; x++) {
                d[y][x] = new C3(rawImage.getRGB(x, y));
            }
        }

        updatingMap = n;

        for (Y = 0; Y < new_height; Y++) {
            for (X = 0; X < new_width; X++) {

                C3 oldColor = d[Y][X];
                C3 newColor; //, frontColor;

                if(n == 16) {
                    newColor = c3.findClosestPaletteColor(oldColor, sixteenPalette);
                    sixteenImage.setRGB(X, Y, newColor.toColor().getRGB());

                    // Update the map for saving (primary color), we are also getting the secondary color from this
                    sixteenMap[Y][X][0] = c3.findClosestPaletteColorIndex(oldColor, sixteenPalette, eightPalette);

                    // Now we have two colors (primary and secondary), we should figure out the true distance
                    // from the actual pixel in the rawImage next.

                    calculateTrueDistance(X, Y, 16);

                }
                else {
                    newColor = c3.findClosestPaletteColor(oldColor, eightPalette);
                    eightImage.setRGB(X, Y, newColor.toColor().getRGB());

                    // Update the map for saving
                    eightMap[Y][X][0] = c3.findClosestPaletteColorIndex(oldColor, eightPalette, eightPalette);

                    calculateTrueDistance(X, Y, 8);
                }

                C3 err = oldColor.sub(newColor);

                if (X + 1 < new_width)
                    d[Y][X + 1] = d[Y][X + 1].add(err.mul( (7. / 16) * percentage ));

                if (X - 1 >= 0 && Y + 1 < new_height)
                    d[Y + 1][X - 1] = d[Y + 1][X - 1].add(err.mul( (3. / 16) * percentage ));

                if (Y + 1 < new_height)
                    d[Y + 1][X] = d[Y + 1][X].add(err.mul( (5. / 16) * percentage));

                if (X + 1 < new_width && Y + 1 < new_height)
                    d[Y + 1][X + 1] = d[Y + 1][X + 1].add(err.mul( (1. / 16) * percentage));
            } // x ends


        } // y ends

        // -----------------------------------------------------------------------------------------

        new_height *= 2;

        if(n == 16) {
            renderHTML();

            Image newImg = sixteenImage.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_REPLICATE);
            ImageIcon imageIcon = new ImageIcon(newImg);  // transform it back
            secondLabel.setIcon(imageIcon);
            secondLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        }
        else {
            Image newImg = eightImage.getScaledInstance(new_width, new_height, java.awt.Image.SCALE_REPLICATE);
            ImageIcon imageIcon = new ImageIcon(newImg);  // transform it back
            thirdLabel.setIcon(imageIcon);
            thirdLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        }
    }

    // ===============================================================================================================

    public void renderHTML() {

        // Render the HTML

        C3 bgColor, fgColor;
        double normalization;

        int width = rawImage.getWidth();
        int height = rawImage.getHeight();

        StringBuilder body = new StringBuilder();

        // Reset the HTML
        HTML = "";

        for(Y = 0; Y < height; Y++) {
            for(X = 0; X < width; X++) {

                bgColor = sixteenPalette[ sixteenMap[Y][X][0] ];
                fgColor = sixteenPalette[ sixteenMap[Y][X][1] ];

                // We now have the sixteenFactor we can use to find a suitable character

                if(sixteenFactor[Y][X] > 1.0 && sixteenFactor[Y][X] <= asciiAmount) {
                    normalization = ( asciiAmount - sixteenFactor[Y][X] ) / ( asciiAmount - 1.0 );
                } else
                    normalization = 1.0;  // Or whatever gives a space

                // Find the character belonging to this
                int index = (int)( ASCII_CHARS.length() * normalization ) - 1;

                if(index < 0)
                    index = 0;

                String ascii = ASCII_CHARS.substring( index, index + 1 );

                sixteenCharacterMap[Y][X] = ascii;

                if(ascii.equals("<")) {
                    ascii = "&lt;";
                }

                if(ascii.equals(" ")) {
                    ascii = "&nbsp;";
                }

                body.append( "<span style=\"color:");
                body.append(Integer.toHexString( fgColor.toColor().getRGB() ).substring(2));

                body.append("; background-color:");
                body.append(Integer.toHexString( bgColor.toColor().getRGB() ).substring(2));
                body.append("\">" + ascii + "</span>" );

                // Save the eight character map as well here

                if(eightFactor[Y][X] > 1.0 && eightFactor[Y][X] <= asciiAmount) {
                    normalization = ( asciiAmount - eightFactor[Y][X] ) / ( asciiAmount - 1.0 );
                } else
                    normalization = 1.0;  // Or whatever gives a space

                // Find the character belonging to this
                index = (int)( ASCII_CHARS.length() * normalization ) - 1;

                if(index < 0)
                    index = 0;

                ascii = ASCII_CHARS.substring( index, index + 1 );

                eightCharacterMap[Y][X] = ascii;

            } // X ends

            body.append("<br>\n");
        } // Y ends

        saveHTML = HTML_before + body + HTML_after;
        textPane.setText(saveHTML);
        textPane.setCaretPosition(0);

        setStatusLabel();
    }

    // ===============================================================================================================

    public void actionEffect(int type) throws IOException {

        if(type == 0) { // Sharpen

            // Has to add up to 1.0
            float[] sharpKernel = {
                    0.0f, -0.25f, 0.0f,
                    -0.25f, 2.0f, -0.25f,
                    0.0f, -0.25f, 0.0f
            };

            BufferedImageOp sharpen = new ConvolveOp(
                    new Kernel(3, 3, sharpKernel),
                    ConvolveOp.EDGE_NO_OP, null);

            BufferedImage tempImage = sharpen.filter(rawImage, null);

            loadImage(tempImage, false);
        }
        else if(type == 1) { // Blur

            float ninth = 1.0f / 9.0f;
            float[] blurKernel = {
                    ninth, ninth, ninth,
                    ninth, ninth, ninth,
                    ninth, ninth, ninth
            };

            BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));
            BufferedImage tempImage = blur.filter(rawImage, null);

            loadImage(tempImage, false);
        }
        else if(type == 2) { // Brighten

            BufferedImageOp brighten = new RescaleOp(1.1f, 0, null);
            BufferedImage tempImage = brighten.filter(rawImage, null);

            loadImage(tempImage, false);
        }
        else if(type == 3) { // Darken

            BufferedImageOp darken = new RescaleOp(0.9f, 0, null);
            BufferedImage tempImage = darken.filter(rawImage, null);

            loadImage(tempImage, false);
        }
        else if(type == 4) { // Monochrome

            BufferedImage greyImage = new BufferedImage(
                    rawImage.getWidth(),
                    rawImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D graphic = greyImage.createGraphics();
            graphic.drawImage(rawImage, 0, 0, Color.WHITE, null);

            for (int i = 0; i < greyImage.getHeight(); i++) {
                for (int j = 0; j < greyImage.getWidth(); j++) {
                    Color c = new Color(greyImage.getRGB(j, i));
                    int red = (int) (c.getRed() * 0.299);
                    int green = (int) (c.getGreen() * 0.587);
                    int blue = (int) (c.getBlue() * 0.114);

                    Color newColor = new Color(
                            red + green + blue,
                            red + green + blue,
                            red + green + blue);

                    greyImage.setRGB(j, i, newColor.getRGB());
                }
            }

            loadImage(greyImage, false);
        }
        else if(type == 5) {  // Invert

            LookupTable lookup = new LookupTable(0, 4)
            {
                @Override
                public int[] lookupPixel(int[] src, int[] dest)
                {
                    dest[0] = 255 - src[0];
                    dest[1] = 255 - src[1];
                    dest[2] = 255 - src[2];
                    return dest;
                }
            };

            LookupOp op = new LookupOp(lookup, new RenderingHints(null));
            BufferedImage invertedImage = op.filter(rawImage, null);
            loadImage(invertedImage, false);
        }

        firstLabel.requestFocus();  // To keep button from appearing pressed
    }

    // ===============================================================================================================

    public void levelOKAction() {
        undoImage = backupRawImage;
    }

    public void levelCancelAction() throws IOException {
        loadImage(backupRawImage, false);
        undoImage = backupUndoImage;
    }

    void levelsAction() {

        // Backup the buffered images now

        backupRawImage = rawImage;
        backupUndoImage = undoImage;

        lev = new Levels(this, rawImage);

        firstLabel.requestFocus();  // To keep button from appearing pressed
    }

    // ===============================================================================================================

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() throws IOException, BadLocationException {

        //Create and set up the window.
        ANSI_generator frame = new ANSI_generator(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Rectangle screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        int frameWidth = 745, frameHeight = screenSize.height; // 802

        //Set up the content pane.
        frame.addComponentsToPane(frame.getContentPane());

        URL url = ANSI_generator.class.getResource("images/icon.png");
        ImageIcon icon = new ImageIcon(url);

        frame.setIconImage(icon.getImage());

        frame.setBounds(0, 0, frameWidth, frameHeight);

        frame.setMinimumSize(new Dimension(frameWidth, 550));
        frame.setPreferredSize(new Dimension(frameWidth, frameHeight));

        frame.setSize(frameWidth, frameHeight);

        // Display the window
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);  // false

        frame.setLocationRelativeTo(null);  // This centers the app

        frame.requestFocus();
    }

    public static void main(String[] args) {

        FlatDarculaLaf.install();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
