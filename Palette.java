public class Palette {

    ANSI_generator ANSI;

    public C3[] getPalette(int type, boolean full) {
        if(full) {  // Full palette
            if(type == 0) {  // ANSI
                return
                        new C3[] {
                                new C3(  0,   0,   0),    // 0 black
                                new C3(  128,   0,   0),  // 1 red
                                new C3(  0,   128,   0),  // 2 green
                                new C3(  128,   128,   0),  // 3 yellow
                                new C3(  0,   0,   128),  // 4 blue
                                new C3(  128,   0,   128),  // 5 magenta
                                new C3(  0,   128,   128),  // 6 cyan
                                new C3(  192,   192,   192),  // 7 white

                                new C3(  128,   128,   128),  // 8 bright black
                                new C3(  255,   0,   0),  // 9 bright red
                                new C3(  0,   255,   0),  // 10 bright green
                                new C3(  255,   255,   0),  // 11 bright yellow
                                new C3(  0,   0,   255),  // 12 bright blue
                                new C3(  255,   0,   255),  // 13 bright magenta
                                new C3(  0,   255,   255),  // 14 bright cyan
                                new C3(  255,   255,   255),  // 15 bright white
                        };
            }
            else if(type == 1) {  // Git Bash
                return
                        new C3[] {
                                new C3(  0,   0,   0),    // 0 black
                                new C3(  191,   0,   0),  // 1 red
                                new C3(  0,   191,   0),  // 2 green
                                new C3(  191,   191,   0),  // 3 yellow
                                new C3(  0,   0,   191),  // 4 blue
                                new C3(  191,   0,   191),  // 5 magenta
                                new C3(  0,   191,   191),  // 6 cyan
                                new C3(  191,   191,   191),  // 7 white

                                new C3(  64,   64,   64),  // 8 bright black
                                new C3(  255,   64,   64),  // 9 bright red
                                new C3(  64,   255,   64),  // 10 bright green
                                new C3(  255,   255,   64),  // 11 bright yellow
                                new C3(  96,   96,   255),  // 12 bright blue
                                new C3(  255,   64,   255),  // 13 bright magenta
                                new C3(  64,   255,   255),  // 14 bright cyan
                                new C3(  255,   255,   255),  // 15 bright white
                        };

            }
            else if(type == 2) {  // mIRC
                return
                        new C3[] {
                                new C3(  255,   255,   255),    // 0 White
                                new C3(  0,   0,   0),  // 1 Black
                                new C3(  0,   0,   127),  // 2 Blue
                                new C3(  0, 147, 0 ),  // 3 Green
                                new C3(  255, 0, 0 ),  // 4 Light Red
                                new C3(  127, 0, 0 ),  // 5 Brown
                                new C3(  156,   0,   156),  // 6 Purple
                                new C3(  252, 127, 0 ),  // 7 Orange

                                new C3(  255, 255, 0),  // 8 Yellow
                                new C3(  0, 252, 0),  // 9 Light Green
                                new C3(  0, 147, 147),  // 10 Cyan
                                new C3(  0, 255, 255),  // 11 Light Cyan
                                new C3(  0, 0, 252 ),  // 12 Light Blue
                                new C3(  255, 0, 255),  // 13 Pink
                                new C3(  127, 127, 127),  // 14 Grey
                                new C3(  210, 210, 210),  // 15 Light Grey
                        };
            }
        }
        else {  // Half palette

            if(type == 0) {  // ANSI
                return
                        new C3[] {
                                new C3(  0,   0,   0),    // 0 black
                                new C3(  128,   0,   0),  // 1 red
                                new C3(  0,   128,   0),  // 2 green
                                new C3(  128,   128,   0),  // 3 yellow
                                new C3(  0,   0,   128),  // 4 blue
                                new C3(  128,   0,   128),  // 5 magenta
                                new C3(  0,   128,   128),  // 6 cyan
                                new C3(  192,   192,   192),  // 7 white
                        };
            }
            else if(type == 1) {  // Git Bash
                return
                        new C3[] {
                                new C3(  0,   0,   0),    // 0 black
                                new C3(  191,   0,   0),  // 1 red
                                new C3(  0,   191,   0),  // 2 green
                                new C3(  191,   191,   0),  // 3 yellow
                                new C3(  0,   0,   191),  // 4 blue
                                new C3(  191,   0,   191),  // 5 magenta
                                new C3(  0,   191,   191),  // 6 cyan
                                new C3(  191,   191,   191),  // 7 white
                        };
            }
            else if(type == 2) {  // mIRC
                return
                        new C3[] {
                                new C3(  255,   255,   255),    // 0 White
                                new C3(  0,   0,   0),  // 1 Black
                                new C3(  0,   0,   127),  // 2 Blue
                                new C3(  0, 147, 0 ),  // 3 Green
                                new C3(  255, 0, 0 ),  // 4 Light Red
                                new C3(  127, 0, 0 ),  // 5 Brown
                                new C3(  156,   0,   156),  // 6 Purple
                                new C3(  252, 127, 0 ),  // 7 Orange
                        };
            }
        }
        return new C3[] { new C3(0,0,0), new C3(255, 255, 255) };
    }

    public void addParent(ANSI_generator a) {
        ANSI = a;  // Tell FileHandling about Paint.java
    }
}
