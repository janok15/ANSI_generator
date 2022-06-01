import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 *  FileHandling.java
 *
 *  FileHandling opens files in the following file formats:
 *  *.BMP, *.PNG, *.GIF and *.JPEG
 *
 *  @author Jan Øyvind Kruse
 *  @version 1.0
 */

public class FileHandling {

    ANSI_generator ANSI;
    JFileChooser fc;
    File file, currentDirectory;
    String suffix, desc, name;
    boolean overwriteFile;

    public BufferedImage openFile() {

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files (*.BMP, *.PNG, *.GIF, *.JPG, *.JPEG)",
                "bmp", "png", "gif", "jpg", "jpeg");

        fc = new JFileChooser();
        fc.setCurrentDirectory( currentDirectory );  // Sets directory to the last one used in KB Paint this session, if any.

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showOpenDialog(ANSI);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();

            try {

                BufferedImage img = ImageIO.read(file);

                // do something here with the ANSI generator
                //parentANSI.setCanvasName( file.getName() );

                currentDirectory = fc.getCurrentDirectory();

                return img;

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    // =================================================================================================================

    void checkFile() {

        file = fc.getSelectedFile();

        if (fc.getFileFilter() instanceof FileNameExtensionFilter) {

            String[] exts = ((FileNameExtensionFilter)fc.getFileFilter()).getExtensions();
            String nameLower = file.getName().toLowerCase();

            desc = fc.getFileFilter().getDescription();
            suffix = exts[0];

            System.out.println("exts[0]: " + exts[0]);
            System.out.println("exts2: " + desc);
            System.out.println("nameLower: " + nameLower );

            for (String ext : exts) { // check if the file already has a valid extension

                if(!nameLower.endsWith('.' + ext.toLowerCase())) {
                    // if it does not, append the first extension from the selected filter
                    file = new File(file.toString() + '.' + exts[0]);
                }
            }
        }
    }

    // =================================================================================================================

    /**
     * Encode a Character like ä to \u00e4
     *
     * @param ch
     * @return
     */
    public String native2ascii(char ch) {

        StringBuilder sb = new StringBuilder();
        // write \udddd
        sb.append("\\u");
        StringBuffer hex = new StringBuffer(Integer.toHexString(ch));
        hex.reverse();
        int length = 4 - hex.length();
        for (int j = 0; j < length; j++) {
            hex.append('0');
        }
        for (int j = 0; j < 4; j++) {
            sb.append(hex.charAt(3 - j));
        }
        return sb.toString();
    }

    // =================================================================================================================

    public int getEncoding() {
        if(ANSI.mudMenuItem.isSelected()) return 0;  // KoBra MUD
        if(ANSI.shellMenuItem.isSelected()) return 1;  // UNIX Shell
        return 2;  // mIRC
    }

    public String getCharacter(int previous, int current, int previousFG, int currentFG, String ASCII) {

        String returnString = "";

        int encoding = getEncoding();

        if(encoding == 1)  // UNIX Shell
            ASCII = native2ascii(ASCII.charAt(0));

        if(encoding == 2) {
            switch (ASCII) {
                case " " : ASCII = " "; break;
                case "8" : ASCII = "\u0038"; break;
                case "0" : ASCII = "\u0030"; break;
                case "1": ASCII = "\u0031"; break;
            }
        }

        // Do not alter the escape codes if the previous foreground and background colors were identical
        if(previous == current && previousFG == currentFG) {
            return ASCII;
        }

        if(previous < 8 && current >= 8) {
            // Previous was not bright, but this is bright - Turn on inverse
            if(encoding == 0)  // KoBra MUD
                returnString += "\u0010S";
            else if(encoding == 1)  // UNIX Shell
                returnString += "\\e[7m";
        }

        if(previous >= 8 && current < 8) {
            // Previous was bright, but this is not bright - Turn off inverse
            if(encoding == 0) // KoBra MUD
                returnString += "\u0010s";
            else if(encoding == 1)  // UNIX Shell
                returnString += "\\e[27m";
        }

        // If the previous background color is different from the current, we have to set a new one
        if(previous != current) {
            if(encoding == 0) {  // KoBra MUD
                switch (current) {
                    case 0 : returnString += "\u0010_0"; break;
                    case 1 : returnString += "\u0010_1"; break;
                    case 2 : returnString += "\u0010_2"; break;
                    case 3 : returnString += "\u0010_3"; break;
                    case 4 : returnString += "\u0010_4"; break;
                    case 5 : returnString += "\u0010_5"; break;
                    case 6 : returnString += "\u0010_6"; break;
                    case 7 : returnString += "\u0010_7"; break;

                    case 8 : returnString += "\u00100"; break;
                    case 9 : returnString += "\u00101"; break;
                    case 10 : returnString += "\u00102"; break;
                    case 11 : returnString += "\u00103"; break;
                    case 12 : returnString += "\u00104"; break;
                    case 13 : returnString += "\u00105"; break;
                    case 14 : returnString += "\u00106"; break;
                    case 15 : returnString += "\u00107"; break;

                    default : returnString += "X"; break;
                }
            }
            else if(encoding == 1) {  // UNIX Shell
                switch(current) {
                    case 0 : returnString += "\\e[40m"; break;
                    case 1 : returnString += "\\e[41m"; break;
                    case 2 : returnString += "\\e[42m"; break;
                    case 3 : returnString += "\\e[43m"; break;
                    case 4 : returnString += "\\e[44m"; break;
                    case 5 : returnString += "\\e[45m"; break;
                    case 6 : returnString += "\\e[46m"; break;
                    case 7 : returnString += "\\e[47m"; break;

                    case 8 : returnString += "\\e[30m"; break;
                    case 9 : returnString += "\\e[31m"; break;
                    case 10 : returnString += "\\e[32m"; break;
                    case 11 : returnString += "\\e[33m"; break;
                    case 12 : returnString += "\\e[34m"; break;
                    case 13 : returnString += "\\e[35m"; break;
                    case 14 : returnString += "\\e[36m"; break;
                    case 15 : returnString += "\\e[37m"; break;

                    default : returnString += "X"; break;
                }
            }  // UNIX Shell ends
            else if(encoding == 2) {  // mIRC
                returnString += "\u0003" + (currentFG < 10 ? "0" + currentFG : currentFG) + "," + (current < 10 ? "0" + current : current);
            }  // mIRC ends
        }

        boolean changeForeground = false;

        if(previous == -1)  // New line
            changeForeground = true;
        else if(previous < 8 && current >= 8) // Previous BG was not inverse, but the current BG is
            changeForeground = true;
        else if(previous >= 8 && current < 8) // Previous BG was inverse, but the current BG is not
            changeForeground = true;
        else if(previousFG != currentFG) // The previous foreground color is different from the current
            changeForeground = true;

        if(changeForeground) {

            if(encoding == 0) {  // KoBra MUD
                if(current >= 8) {  // Inverse
                    switch (currentFG) {
                        case 0 : returnString += "\u0010_0"; break;
                        case 1 : returnString += "\u0010_1"; break;
                        case 2 : returnString += "\u0010_2"; break;
                        case 3 : returnString += "\u0010_3"; break;
                        case 4 : returnString += "\u0010_4"; break;
                        case 5 : returnString += "\u0010_5"; break;
                        case 6 : returnString += "\u0010_6"; break;
                        case 7 : returnString += "\u0010_7"; break;
                    }
                }
                else { // Non inverse
                    switch (currentFG) {
                        case 0 : returnString += "\u00100"; break;
                        case 1 : returnString += "\u00101"; break;
                        case 2 : returnString += "\u00102"; break;
                        case 3 : returnString += "\u00103"; break;
                        case 4 : returnString += "\u00104"; break;
                        case 5 : returnString += "\u00105"; break;
                        case 6 : returnString += "\u00106"; break;
                        case 7 : returnString += "\u00107"; break;
                    }
                }
            }
            else if(encoding == 1) {  // UNIX Shell
                if (current >= 8) {  // Inverse
                    switch (currentFG) {
                        case 0: returnString += "\\e[40m"; break;
                        case 1: returnString += "\\e[41m"; break;
                        case 2: returnString += "\\e[42m"; break;
                        case 3: returnString += "\\e[43m"; break;
                        case 4: returnString += "\\e[44m"; break;
                        case 5: returnString += "\\e[45m"; break;
                        case 6: returnString += "\\e[46m"; break;
                        case 7: returnString += "\\e[47m"; break;
                    }
                } else { // Non inverse
                    switch (currentFG) {
                        case 0: returnString += "\\e[30m"; break;
                        case 1: returnString += "\\e[31m"; break;
                        case 2: returnString += "\\e[32m"; break;
                        case 3: returnString += "\\e[33m"; break;
                        case 4: returnString += "\\e[34m"; break;
                        case 5: returnString += "\\e[35m"; break;
                        case 6: returnString += "\\e[36m"; break;
                        case 7: returnString += "\\e[37m"; break;
                    }
                }
            } // UNIX Shell ends
            else if(encoding == 2) {  // mIRC

                // We only need to change the foreground by itself if the previous background was the same
                // as the current background, otherwise we have already updated the foreground color
                if(previous == current)
                    returnString += "\u0003" + (currentFG < 10 ? "0" + currentFG : currentFG);
            }  // mIRC ends
        }

        return returnString + ASCII;
    }

    // =================================================================================================================

    public String getANSI(int height, int n) {

        StringBuilder temp = new StringBuilder();
        String ascii;

        int previousChar, currentChar = 0, lineHasBright;
        int previousCharFG, currentCharFG;

        int encoding = getEncoding();

        if(encoding == 1)  // UNIX Shell
            temp.append("#!/bin/bash\n");
        else if(encoding == 2)  // mIRC
            temp.append("alias ansi {\n");

        for(int y = 0; y < height; y++) {

            lineHasBright = 0;  // This line has not yet used a bright character

            if(encoding == 1)  // UNIX Shell
                temp.append("echo -e \"");
            else if(encoding == 2)
                temp.append("  echo ");

            //temp.append("  msg $chan ");


            for(int x = 0; x < ANSI.WIDTH; x++) {

                // This should use the characterMap as well...

                if(x > 0) {

                    if(n == 16) {
                        previousChar = ANSI.sixteenMap[y][x - 1][0];
                        previousCharFG = ANSI.sixteenMap[y][x - 1][1];
                    }
                    else {
                        previousChar = ANSI.eightMap[y][x - 1][0];
                        previousCharFG = ANSI.eightMap[y][x - 1][1];
                    }
                }
                else {
                    previousChar = -1;
                    previousCharFG = -1;
                }

                if(n == 16) {
                    currentChar = ANSI.sixteenMap[y][x][0];
                    currentCharFG = ANSI.sixteenMap[y][x][1];

                    if(currentChar >= 8 && lineHasBright == 0) {

                        // Turn on bold since this line now includes
                        // bright characters

                        if(encoding == 0)  // KoBra MUD
                            temp.append( "\u0010B" );
                        else if(encoding == 1)  // UNIX Shell
                            temp.append("\\e[1m");  // Bold on

                        lineHasBright = 1;
                    }

                    if(currentChar < 8 && lineHasBright == 1) {

                        // Turn off bold since this BG color does not require it

                        if(encoding == 0)  // KoBra MUD
                            temp.append( "\u0010b" );
                        else if(encoding == 1)  // UNIX Shell
                            temp.append("\\e[22m");  // Bold off

                        lineHasBright = 0;
                    }

                    ascii = ANSI.sixteenCharacterMap[y][x];
                }
                else {
                    currentChar = ANSI.eightMap[y][x][0];
                    currentCharFG = ANSI.eightMap[y][x][1];
                    ascii = ANSI.eightCharacterMap[y][x];
                }

                temp.append(getCharacter(previousChar,
                        currentChar,
                        previousCharFG,
                        currentCharFG,
                        ascii)
                );
            }  // x ends

            // If the current character was bright, turn off inverse
            if(currentChar >= 8) {
                if(encoding == 0) // KoBra MUD
                    temp.append("\u0010s");
                else if(encoding == 1)  // UNIX Shell
                    temp.append("\\e[27m");  // Inverse off
            }

            if(encoding == 1)  // UNIX Shell
                temp.append("\\e[0m\"");  // Turn off everything

            temp.append('\n');

        } // y ends

        if(encoding == 2)  // mIRC
            temp.append("}\n");

        return String.valueOf(temp);
    }

    // =================================================================================================================

    public String saveFile(String n, int height) {

        suffix = null;
        name = n;

        fc = new JFileChooser();
        fc.setCurrentDirectory( currentDirectory );

        String customSuffix, customSuffixCap, customDesc, type;

        // TODO ... Should only have three possibilities ( aa/sh/mrc, .txt, .html )

/*
        String[][] extensions = {
                { "16-color AA (*.4AA)", "4aa" },
                { "8-color AA (*.3AA)", "3aa" },
                { "16-color text file (*.4MD)", "4md" },
                { "8-color MUD (*.3MD)", "3md" },
                { "Webpage (*.html)", "html" },
        };
 */

        if(ANSI.mudMenuItem.isSelected()) {
            customSuffix = "aa";
            customSuffixCap = "AA";
            customDesc = "KoBra MUD script";

        }
        else if(ANSI.shellMenuItem.isSelected()) {
            customSuffix = "sh";
            customSuffixCap = "SH";
            customDesc = "Bash script";
        }
        else {
            customSuffix = "mrc";
            customSuffixCap = "MRC";
            customDesc = "mIRC script";
        }

        String[][] extensions = {
                { "16-color " + customDesc + " (*." + customSuffixCap + ")", customSuffix },
                { "8-color " + customDesc + " (*." + customSuffixCap + ")", customSuffix },
                { "16-color text file (*.txt)", "txt" },
                { "8-color text file (*.txt)", "txt" },
                { "Webpage (*.html)", "html" },
        };


        FileNameExtensionFilter[] f = new FileNameExtensionFilter[5]; // 4

        for(int i = 0; i < 5; i++) {  // i < 4

            f[i] = new FileNameExtensionFilter( extensions[i][0], extensions[i][1] );
            fc.addChoosableFileFilter( f[i] );
        }

        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(name));

        if( fc.showSaveDialog( ANSI ) == JFileChooser.APPROVE_OPTION ) {

            boolean doExport = true;
            overwriteFile = false;

            checkFile();

            while( doExport && file.exists() && !overwriteFile ) {

System.out.println("@@ AAA");

                overwriteFile =
                        (JOptionPane.showConfirmDialog(ANSI,
                                "Replace file?",
                                "Export Settings",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);

                if(!overwriteFile) {

System.out.println("@@ BBB");


                    if( fc.showSaveDialog( ANSI ) == JFileChooser.APPROVE_OPTION ) {

                        System.out.println("@@ CCC");

                        checkFile();
                    }
                    else {
                        doExport = false;

                        System.out.println("@@ DDD");

                    }
                }
            }

            if(doExport) {

                try {
                    StringBuilder temp = new StringBuilder();
                    FileWriter myWriter = new FileWriter(file);

                    System.out.println("@@ EEE");


                    switch (desc) {
                        case "Webpage (*.html)":  // html
                            myWriter.write(ANSI.saveHTML);
                            break;
                        case "16-color KoBra MUD script (*.AA)" :
                        case "16-color Bash script (*.SH)" :
                        case "16-color mIRC script (*.MRC)" :
                            myWriter.write(getANSI(height, 16));
                            break;
                        case "8-color KoBra MUD script (*.AA)" :
                        case "8-color Bash script (*.SH)" :
                        case "8-color mIRC script (*.MRC)" :
                            myWriter.write(getANSI(height, 8));
                            break;
                        default:

                            for (int y = 0; y < height; y++) {

                                for (int x = 0; x < ANSI.WIDTH; x++) {
                                    if (desc.equals("16-color text file (*.txt)")) { // 4-bit .txt (simple hex values)
                                        temp.append(Integer.toHexString(ANSI.sixteenMap[y][x][0]));
                                    } else if (desc.equals("8-color text file (*.txt)")) {  // 3-bit .txt (simple hex values)
                                        temp.append(Integer.toHexString(ANSI.eightMap[y][x][0]));
                                    }
                                }  // x ends

                                temp.append('\n');
                            }
                            myWriter.write(temp.toString());
                            break;
                    }

                    myWriter.close();

                    currentDirectory = fc.getCurrentDirectory();

                    // TODO   Strip the suffix from this ...
                    return file.getName();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    // =================================================================================================================

    public void addParent(ANSI_generator a) {
        ANSI = a;  // Tell FileHandling about Paint.java
    }
}
