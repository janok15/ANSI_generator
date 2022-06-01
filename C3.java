import java.awt.*;
import java.util.Arrays;

public class C3 {

    int r, g, b;
    ANSI_generator ANSI;

    public C3(int c) {
        Color color = new Color(c);
        r = color.getRed();
        g = color.getGreen();
        b = color.getBlue();
    }

    public C3(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public C3 add(C3 o) { return new C3(r + o.r, g + o.g, b + o.b); }
    public int clamp(int c) { return Math.max(0, Math.min(255, c)); }

    public double diff(C3 o) {
        int Rdiff = o.r - r;
        int Gdiff = o.g - g;
        int Bdiff = o.b - b;

        return Math.sqrt( Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff );
    }

    public C3 mul(double d) { return new C3((int) (d * r), (int) (d * g), (int) (d * b)); }
    public C3 sub(C3 o) { return new C3(r - o.r, g - o.g, b - o.b); }
    public Color toColor() { return new Color(clamp(r), clamp(g), clamp(b)); }
    public int toRGB() { return toColor().getRGB(); }

    static C3 findClosestPaletteColor(C3 c, C3[] palette) {
        C3 closest = palette[0];

        for (C3 n : palette)
            if (n.diff(c) < closest.diff(c))
                closest = n;

        return closest;
    }

    int findClosestPaletteColorIndex(C3 c, C3[] palette, C3[] halfPalette) {
        C3 closest = palette[0];
        C3 complementary = palette[0];  // Remainder when subtracting color closest from color c

        int index = 0, secondary_index = 0;
        double dist, complementary_dist;

        for (C3 n : palette) {

            dist = n.diff(c);

            if(dist < closest.diff(c)) {

                closest = n;

                double factor = 0.20 + 0.20 * ANSI.subtract;

                // Calculate the remainder when using this color, and then find the closest color to remainder

                // 100% reduction
                //complementary = new C3(Math.max(0, c.r - n.r), Math.max(0, c.g - n.g), Math.max(0, c.b - n.g));

                // 75% reduction
                //complementary = new C3(Math.max(0, c.r - 3 * n.r / 4), Math.max(0, c.g - 3 * n.g / 4), Math.max(0, c.b - 3 * n.g / 4));

                // 50% reduction
                //complementary = new C3(Math.max(0, c.r - n.r / 2), Math.max(0, c.g - n.g / 2), Math.max(0, c.b - n.g / 2));

                // 25% reduction
                //complementary = new C3(Math.max(0, c.r - n.r / 4), Math.max(0, c.g - n.g / 4), Math.max(0, c.b - n.g / 4));

                complementary = new C3(Math.max(0, (int)(c.r - n.r * factor)), Math.max(0, (int)(c.g - n.g * factor)), Math.max(0, (int)(c.b - n.g * factor)));

                index = Arrays.asList(palette).indexOf(n);
            }
        }

        closest = halfPalette[0];

        for (C3 n : halfPalette) {

            // Find distance from this palette color to the remainder
            complementary_dist = n.diff(complementary);

            if(complementary_dist < closest.diff(complementary)) {
                closest = n;
                secondary_index = Arrays.asList(halfPalette).indexOf(n);
            }
        }

        if(ANSI.updatingMap == 16) {  // Updating 16 color map
            ANSI.sixteenMap[ANSI.Y][ANSI.X][1] = secondary_index;
        }
        else {  // Updating 8-color map
            ANSI.eightMap[ANSI.Y][ANSI.X][1] = secondary_index;
        }

        return index;
    }

    public void addParent(ANSI_generator a) {
        ANSI = a;  // Tell FileHandling about Paint.java
    }
}
