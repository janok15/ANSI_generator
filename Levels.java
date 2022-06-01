import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Levels extends JDialog {

    static JDialog levels;
    ANSI_generator ANSI;

    JButton okButton, cancelButton;
    JTextField textField_a, textField_b, textField_c, textField_d, textField_e;

    BufferedImage levelImage;

    LevelSliderPanel levelSliderPanel;
    OutputSliderPanel outputSliderPanel;

    int H_PADDING = 15, V_PADDING = 8;

    // =============================================================================================

    class LevelSliderPanel extends JPanel {

        int width, height;

        // black and white will have their actual values displayed in the text box
        // but the mid value will need to be recalculated in the text box (127 = 1.0 )

        int black, mid, white;
        double midPercentage;  // Normalized value of mid thumb position between black and white

        int h_pad, v_pad;

        int claim;  // Which of the three sliders is currently claimed

        public LevelSliderPanel(int width, int height) {
            setSize(width, height);
            this.width = width;
            this.height = height;

            this.midPercentage = 0.5;

            this.black = 0;
            this.white = 255;

            this.mid = normalizeMid();

            this.h_pad = 14;
            this.v_pad = 15;

            this.claim = -1;
        }

        public double midValue(int reversed) {

            double midValue;
            double percentage = this.midPercentage;

            if(reversed == 1)
                percentage = Math.abs(percentage - 1.0);

            if(this.midPercentage < 0.5) {

                if(percentage < 0.005)
                    percentage = 0.0;

                midValue = 9.99 * Math.pow( (1.0 / Math.pow(9.99, 2)), percentage );
            }
            else {
                midValue = 1.9 - 1.8 * percentage;
            }

            if(midValue < 0.1)
                midValue = 0.1;

            return midValue;
        }

        public int normalizeMid() {
            int temp;

            temp = (int)Math.round(this.black +  (this.white - this.black - 2) * midPercentage );

            return temp;
        }

        public void updatePercentage() {

            this.midPercentage = (double)(this.mid - this.black) / (this.white - this.black - 2);

            if(this.midPercentage < 0.0)
                this.midPercentage = 0.0;
            else if(this.midPercentage > 1.0)
                this.midPercentage = 1.0;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(60, 63, 65));
            g2.fillRect(0, 0, this.width, this.height);

            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(100, 100, 100));
            g2.drawLine(this.h_pad, this.v_pad, this.width - this.h_pad - 1, this.v_pad);

            int radius = 13;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the black thumb
             g2.setColor(Color.black);
            //g2.setColor(new Color(37,68,100));
            Ellipse2D.Double circle = new Ellipse2D.Double(
                    this.h_pad + this.black - radius / 2.0,
                    this.v_pad - radius / 2.0, radius, radius);
            g2.fill(circle);

            // Draw the mid thumb
            g2.setColor(Color.gray);
            //g2.setColor(new Color(74,136,199));
            circle = new Ellipse2D.Double(
                    this.h_pad + this.mid - radius / 2.0,
                    this.v_pad - radius / 2.0, radius, radius);
            g2.fill(circle);

            // Draw the white thumb
            g2.setColor(Color.white);
            //g2.setColor(new Color(165,196,227));
            circle = new Ellipse2D.Double(
                    this.h_pad + this.white - radius / 2.0,
                    this.v_pad - radius / 2.0, radius, radius);
            g2.fill(circle);

        }

        public int getClosest(int x) {

            x = x - this.h_pad;

            // Need to figure out which thumb is closest and 'claim' that

            int[] delta = new int[3];

            delta[0] = Math.abs(x - this.black);
            delta[1] = Math.abs(x - this.mid);
            delta[2] = Math.abs(x - this.white);

            int min_distance = 500, closest = -1;

            for(int i = 0; i < 3; i++) {
                if(delta[i] < min_distance) {
                    min_distance = delta[i];
                    closest = i;
                }
            }

            return closest;
        }

        public void updateThumb(int x) throws IOException {

            x -= h_pad;

            // Set boundaries here for allowed values [0, 255]

            if(x < 0) x = 0;
            if(x > 255) x = 255;


            // Check boundaries for black, mid, and white
            if(claim == 0) {  // Boundary for black

                if(x > this.white - 2)
                    x = this.white - 2;
            }
            else if(claim == 1) {  // Boundary for mid

                if(x < this.black)
                    x = this.black;
                else if(x > this.white)
                    x = this.white;

            }
            else {  // Boundary for white
                if(x < this.black + 2)
                    x = this.black + 2;
            }

            switch(this.claim) {
                case 0 :
                    this.black = x;
                    this.mid = normalizeMid();

                    break;
                case 1 :
                    this.mid = x;

                    updatePercentage();

                    break;
                case 2 :
                    this.white = x;
                    this.mid = normalizeMid();

                    break;
            }

            updateLevelTextFields();  // Put new value in the text field

            // Update the image here
            updateImage();

            levelSliderPanel.repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.width, this.height);
        }
    }
    // =============================================================================================

    class LevelListener extends MouseInputAdapter {

        public void mouseDragged(MouseEvent e) {
            int x;

            x = e.getX();
            // We know which one is closest, should update the value of that now
            try {
                levelSliderPanel.updateThumb(x);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void mousePressed(MouseEvent e) {

            // We know which one is closest, should update the value of that now

            levelSliderPanel.claim = levelSliderPanel.getClosest(e.getX());

            try {
                levelSliderPanel.updateThumb(e.getX());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void mouseReleased(MouseEvent e) {

            // Release the claim on the given thumb
            levelSliderPanel.claim = -1;
        }
    }

    // =============================================================================================

    class OutputSliderPanel extends JPanel {

        int width, height;

        // black and white will have their actual values displayed in the text box
        // but the mid value will need to be recalculated in the text box (127 = 1.0 )

        int black, white;
        int h_pad, v_pad;
        int claim;  // Which of the three sliders is currently claimed

        public OutputSliderPanel(int width, int height) {
            setSize(width, height);
            this.width = width;
            this.height = height;

            this.black = 0;
            this.white = 255;

            this.h_pad = 14;
            this.v_pad = 15;

            this.claim = -1;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(60, 63, 65));
            g2.fillRect(0, 0, this.width, this.height);

            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(100, 100, 100));
            g2.drawLine(this.h_pad, this.v_pad, this.width - this.h_pad - 1, this.v_pad);

            int radius = 13;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the black thumb
            g2.setColor(Color.black);

            Ellipse2D.Double circle = new Ellipse2D.Double(
                    this.h_pad + this.black - radius / 2.0,
                    this.v_pad - radius / 2.0, radius, radius);
            g2.fill(circle);

            // Draw the white thumb
            g2.setColor(Color.white);
            circle = new Ellipse2D.Double(
                    this.h_pad + this.white - radius / 2.0,
                    this.v_pad - radius / 2.0, radius, radius);
            g2.fill(circle);
        }

        public int getClosest(int x) {

            x = x - this.h_pad;

            // Need to figure out which thumb is closest and 'claim' that

            int[] delta = new int[2];

            delta[0] = Math.abs(x - this.black);
            delta[1] = Math.abs(x - this.white);

            int min_distance = 500, closest = -1;

            for(int i = 0; i < 2; i++) {
                if(delta[i] < min_distance) {
                    min_distance = delta[i];
                    closest = i;
                }
            }

            return closest;
        }

        public void updateThumb(int x) throws IOException {

            x -= h_pad;

            // Set boundaries here for allowed values [0, 255]

            if(x < 0) x = 0;
            if(x > 255) x = 255;

            switch(this.claim) {
                case 0 :
                    this.black = x;
                    break;
                case 1 :
                    this.white = x;
                    break;
            }

            updateOutputTextFields();  // Put new value in the text field

            // Update the image here
            updateImage();

            outputSliderPanel.repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.width, this.height);
        }
    }
    // =============================================================================================

    class OutputListener extends MouseInputAdapter {

        public void mouseDragged(MouseEvent e) {
            int x;

            x = e.getX();

            // We know which one is closest, should update the value of that now
            try {
                outputSliderPanel.updateThumb(x);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void mousePressed(MouseEvent e) {

            int x;

            x = e.getX();

            // We know which one is closest, should update the value of that now

            outputSliderPanel.claim = outputSliderPanel.getClosest(e.getX());
            try {
                outputSliderPanel.updateThumb(x);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void mouseReleased(MouseEvent e) {

            // Release the claim on the given thumb
            outputSliderPanel.claim = -1;
        }
    }

    // =============================================================================================

    static class HistogramPanel extends JPanel {

        int width, height, num_pixels, maximum;
        BufferedImage bi;

        //int[] brightness = new int[256];
        int[] rgb = new int[256];

        int[] red = new int[256];
        int[] green = new int[256];
        int[] blue = new int[256];

        public HistogramPanel(BufferedImage bi, int width, int height) {

            setSize(width, height);

            this.width = width;
            this.height = height;
            this.bi = bi;

            // Make an array with the brightness values (0-255), and how many there are ...

            int x, y;

            x = this.bi.getWidth();
            y = this.bi.getHeight();

            num_pixels = x * y;

            Color color;

            for(int row = 0; row < y; row++) {

                for(int column = 0; column < x; column++) {

                    // Get this pixel value RGB, and get the brightness (R+G+B)/3
                    color = new Color( this.bi.getRGB(column, row) );

                    red[color.getRed()]++;
                    green[color.getGreen()]++;
                    blue[color.getBlue()]++;
                }
            }

            for(int i = 0; i < 256; i++) {

                rgb[i] = red[i] + green[i] + blue[i];

                if(rgb[i] > maximum)
                    maximum = rgb[i];
            }
        }

        public void paint(Graphics g) {

            g.setColor(new Color(60, 63, 65) );
            g.fillRect(0, 0, this.width, this.height);
            g.setColor(Color.yellow);

            double height;

            for(int i = 0; i < 256; i++) {

                height = rgb[i] / (maximum / 100.0);
                g.fillRect(4 + i, 100 - (int)height, 1, (int)height);

            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.width, this.height);
        }
    }


    // =============================================================================================

    static class GradientPanel extends JPanel {

        int width, height;

        public GradientPanel(int width, int height) {
            setSize(width, height);
            this.width = width;
            this.height = height;
        }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint blackToWhite = new GradientPaint(0, 0,
                    Color.black, this.width, this.height, Color.white);
            g2.setPaint(blackToWhite);
            g2.fillRect(0, 0, this.width, this.height);

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(this.width, this.height);
        }
    }

    public static BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
    // =============================================================================================

    public Levels(ANSI_generator controller, BufferedImage img) {

        ANSI = controller;
        levelImage = copyImage(img);

        final JPanel levelsPanel = new JPanel();

        levels = new JDialog(ANSI, "RGB Levels");

        //levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.PAGE_AXIS));
        levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.LINE_AXIS));  // ??

        //levelsPanel.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);

        // Add padding to the left side
        levelsPanel.add(Box.createRigidArea(new Dimension(H_PADDING, 0)));

        // =========================================================================================

        // Add the main contents

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.PAGE_AXIS));

        // Add padding to the top
        midPanel.add(Box.createRigidArea(new Dimension(0, V_PADDING)));

        // -----------------------------------------------------------------------------------------

        // The histogramPanel needs to be inside another panel

        JPanel borderPanel_1 = new JPanel(new BorderLayout());
        borderPanel_1.setMaximumSize(new Dimension(284, 120));

        // Need to get the raw image from ANSI here and send it to histogramPanel
        HistogramPanel histogramPanel = new HistogramPanel(controller.rawImage,
                264, 100);

        borderPanel_1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Input Levels:"),
                BorderFactory.createEmptyBorder(4,4,4,4)));

        borderPanel_1.add(histogramPanel);

        midPanel.add(borderPanel_1);

        // -----------------------------------------------------------------------------------------

        levelSliderPanel = new LevelSliderPanel(284, 30);

        LevelListener levelListener = new LevelListener();

        levelSliderPanel.addMouseListener(levelListener);
        levelSliderPanel.addMouseMotionListener(levelListener);

        midPanel.add(levelSliderPanel);

        // -----------------------------------------------------------------------------------------

        // Three input boxes on a line

        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(new BoxLayout(textFieldsPanel, BoxLayout.LINE_AXIS));

        textField_a = new JTextField(5);
        textField_b = new JTextField(5);
        textField_c = new JTextField(5);

        textField_a.setMaximumSize(new Dimension(50, 25 ));
        textField_b.setMaximumSize(new Dimension(50, 25 ));
        textField_c.setMaximumSize(new Dimension(50, 25 ));

        textField_a.setEditable(false);
        textField_b.setEditable(false);
        textField_c.setEditable(false);

        textField_a.setEnabled(false);
        textField_b.setEnabled(false);
        textField_c.setEnabled(false);

        int padding = 75;

        textFieldsPanel.add(textField_a);
        textFieldsPanel.add(Box.createRigidArea(new Dimension(padding, 0)));
        textFieldsPanel.add(textField_b);
        textFieldsPanel.add(Box.createRigidArea(new Dimension(padding, 0)));
        textFieldsPanel.add(textField_c);

        updateLevelTextFields();

        midPanel.add(textFieldsPanel);

        // -----------------------------------------------------------------------------------------

        // The gradient  'Output Levels:'

        JPanel borderPanel_2 = new JPanel(new BorderLayout());

        borderPanel_2.setMaximumSize(new Dimension(284, 35));

        GradientPanel gradientPanel = new GradientPanel(264, 25);

        borderPanel_2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Output Levels:"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        borderPanel_2.add(gradientPanel);

        midPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        midPanel.add(borderPanel_2);

        // -----------------------------------------------------------------------------------------

        // Output level slider

        outputSliderPanel = new OutputSliderPanel(284, 30);

        OutputListener outputListener = new OutputListener();

        outputSliderPanel.addMouseListener(outputListener);
        outputSliderPanel.addMouseMotionListener(outputListener);

        midPanel.add(outputSliderPanel);

        // -----------------------------------------------------------------------------------------

        // Two input boxes

        JPanel textFieldsPanel2 = new JPanel();
        textFieldsPanel2.setLayout(new BoxLayout(textFieldsPanel2, BoxLayout.LINE_AXIS));

        textField_d = new JTextField(5);
        textField_e = new JTextField(5);

        textField_d.setMaximumSize(new Dimension(50, 25 ));
        textField_e.setMaximumSize(new Dimension(50, 25 ));

        textField_d.setEditable(false);
        textField_e.setEditable(false);

        textField_d.setEnabled(false);
        textField_e.setEnabled(false);

        textFieldsPanel2.add(textField_d);
        textFieldsPanel2.add(Box.createRigidArea(new Dimension(190, 0)));
        textFieldsPanel2.add(textField_e);

        updateOutputTextFields();

        midPanel.add(textFieldsPanel2);

        // -----------------------------------------------------------------------------------------

        // OK, Cancel buttons

        JPanel buttonPanel = new JPanel();
        FlowLayout buttonLayout = new FlowLayout();

        buttonPanel.setLayout(buttonLayout);
        buttonLayout.setAlignment(FlowLayout.RIGHT);

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        // TODO: Button functionality

        okButton.addActionListener(e -> okAction());
        cancelButton.addActionListener(e -> cancelAction());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        midPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        midPanel.add(buttonPanel);

        levelSliderPanel.setFocusable(true);
        levelSliderPanel.requestFocus();  // To keep button from appearing pressed

        // -----------------------------------------------------------------------------------------

        // Add padding to the bottom
        midPanel.add(Box.createRigidArea(new Dimension(0, V_PADDING)));
        levelsPanel.add(midPanel);

        // =========================================================================================

        // Add padding to the right side
        levelsPanel.add(Box.createRigidArea(new Dimension(H_PADDING, 0)));

        levels.add(levelsPanel);

        // setsize of dialog
        levels.setSize(330, 408); // 450  417
        levels.setResizable(false);

        levels.setLocationRelativeTo(null);  // This centers the app

        URL url = getClass().getResource("images/icon.png");
        ImageIcon imgicon = new ImageIcon(url);
        levels.setIconImage(imgicon.getImage());

        // set modal to true (blocks input to parent) and make the dialog visible
        levels.setModal(true);

        levels.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelAction();
            }
        });

        levels.setVisible(true);
    }

    public void updateLevelTextFields() {

        DecimalFormat df2 = new DecimalFormat("0.00",
                DecimalFormatSymbols.getInstance(Locale.US) );  // #.##

        textField_a.setText(String.valueOf(levelSliderPanel.black));
        textField_b.setText( df2.format(levelSliderPanel.midValue(0)) );
        textField_c.setText(String.valueOf(levelSliderPanel.white));

    }

    public void updateOutputTextFields() {

        textField_d.setText(String.valueOf(outputSliderPanel.black));
        textField_e.setText(String.valueOf(outputSliderPanel.white));
    }

    public void updateImage() throws IOException {

        int x, y;
        int r, g, b;

        double new_r, new_g, new_b;
        Color color;

        BufferedImage tempImage = copyImage(levelImage);

        int shadow = levelSliderPanel.black;
        int highlight = levelSliderPanel.white;
        double gamma = levelSliderPanel.midValue(1);

        int outShadow = outputSliderPanel.black;
        int outHighlight = outputSliderPanel.white;

        for(y = 0; y < levelImage.getHeight(); y++) {
            for(x = 0; x < levelImage.getWidth(); x++) {

                // Apply the input levels:

                // Get the color of this pixel
                color = new Color( levelImage.getRGB(x, y) );

                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();

                new_r = 255 * ( (double)( r - shadow ) / ( highlight - shadow ));
                new_g = 255 * ( (double)( g - shadow ) / ( highlight - shadow ));
                new_b = 255 * ( (double)( b - shadow ) / ( highlight - shadow ));

                if(new_r < 0) new_r = 0;
                if(new_g < 0) new_g = 0;
                if(new_b < 0) new_b = 0;

                if(new_r > 255) new_r = 255;
                if(new_g > 255) new_g = 255;
                if(new_b > 255) new_b = 255;

                if(gamma != 1.0) {
                    new_r = (255 * Math.pow(new_r / 255.0, gamma));
                    new_g = (255 * Math.pow(new_g / 255.0, gamma));
                    new_b = (255 * Math.pow(new_b / 255.0, gamma));
                }

                // TODO: Make this less ugly

                if(new_r < 0) new_r = 0;
                if(new_g < 0) new_g = 0;
                if(new_b < 0) new_b = 0;

                if(new_r > 255) new_r = 255;
                if(new_g > 255) new_g = 255;
                if(new_b > 255) new_b = 255;


                // Apply the output levels:

                new_r = ((new_r / 255.0) * (outHighlight - outShadow) + outShadow);
                new_g = ((new_g / 255.0) * (outHighlight - outShadow) + outShadow);
                new_b = ((new_b / 255.0) * (outHighlight - outShadow) + outShadow);

                if(new_r < 0) new_r = 0;
                if(new_g < 0) new_g = 0;
                if(new_b < 0) new_b = 0;

                if(new_r > 255) new_r = 255;
                if(new_g > 255) new_g = 255;
                if(new_b > 255) new_b = 255;

                color = new Color((int)new_r, (int)new_g, (int)new_b);

                tempImage.setRGB(x, y, color.getRGB());
            } // x ends
        } // y ends

        ANSI.loadImage(tempImage, false);
    }

    public void okAction() {
        levelSliderPanel.requestFocus();  // To keep button from appearing pressed

        ANSI.levelOKAction();

        levels.setVisible(false);
        levels.dispose(); // Destroy the Levels object
    }

    public void cancelAction() {
        levelSliderPanel.requestFocus();  // To keep button from appearing pressed

        try {
            ANSI.levelCancelAction();
        } catch (IOException e) {
            e.printStackTrace();
        }

        levels.setVisible(false);
        levels.dispose(); // Destroy the Levels object
    }
}
