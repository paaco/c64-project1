import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Convert spritesheet to asm

public class Sprite2asm {

    // text output: ACME style uses !byte, KickAssembler expects .byte, DreamAss uses .db
    private static final String PREFIX = "!byte";

    // color specifiers
    private static final Pattern BGPATTERN = Pattern.compile("-bg([0-9a-fA-F])"); // -bgX bg (forces hires)
    private static final Pattern MCPATTERN = Pattern.compile("-mc([0-9a-fA-F])([0-9a-fA-F])"); // -mcXX mc1 mc2

    private Sprite2asm(String fname) {
        srcfilename = fname;
    }

    private final String srcfilename;

    public static void main(String[] args) throws Exception {
        new Sprite2asm(args[0]).run();
    }

    private int pixel_width = 1; // default to hires
    private int bgIndex = 0;     // black, but actually defaults to transparent index
    private int mc1Index = 1;    // white
    private int mc2Index = 2;    // red

    // pixelWidth 1: bgIndex is '0', any other color is '1' (sprite color)
    // pixelWidth 2: bgIndex is '00'(0), mc1Index is '01'(1), mc2Index is '11'(3), any other is '10'(2) (sprite color)
    private byte[] extractSprite(Raster pixels, int xoff, int yoff) {
        byte[] buf = new byte[64];
        int bufoffset = 0;
        int bitcount = 0;
        byte b = 0;
        for (int y = 0; y < 21; y++) {
            for (int x = 0; x < 24; x += pixel_width) {
                int pixel = pixels.getSample(x + xoff, y + yoff, 0);
                b <<= pixel_width;

                if (pixel_width == 1) {
                    // hires
                    if (pixel == bgIndex) b |= 0;
                    else b |= 1;
                } else {
                    // mc
                    if (pixel == bgIndex) b |= 0;
                    else if (pixel == mc1Index) b |= 1;
                    else if (pixel == mc2Index) b |= 3;
                    else b |= 2;
                }

                // flush
                bitcount += pixel_width;
                if (bitcount == 8) {
                    buf[bufoffset++] = b;
                    bitcount = 0;
                    b = 0;
                }
            }
        }
        return buf;
    }

    // extract color format from string
    private void updateColorMapping(String str) {
        Matcher bg = BGPATTERN.matcher(str);
        Matcher mc = MCPATTERN.matcher(str);
        if (bg.find()) { // "-bgX" sets bg index and forces hires
            bgIndex = Integer.parseInt(bg.group(1),16);
            pixel_width = 1;
        }
        if (mc.find()) { // -mcXX sets mc1 and mc2 indices and forces mc
            mc1Index = Integer.parseInt(mc.group(1),16);
            mc2Index = Integer.parseInt(mc.group(2),16);
            pixel_width = 2;
        }
    }

    private void run() throws IOException {
        File f = new File(srcfilename);
        BufferedImage image = ImageIO.read(f);
        int height = image.getHeight();
        int width = image.getWidth();
        if (!(image.getColorModel() instanceof IndexColorModel)) {
            System.err.println("ERROR: image should have palette");
        } else {
            // pick bg from transparent color index
            bgIndex = ((IndexColorModel)image.getColorModel()).getTransparentPixel();
            updateColorMapping(srcfilename);

            System.out.println("; Sprite2asm " + f.getName() + " " + DateFormat.getDateTimeInstance().format(new Date()));
            Raster pixels = image.getData();
            int nr = 0;
            for (int sy = 0; sy + 21 <= height; sy += 21) {
                for (int sx = 0; sx + 24 <= width; sx += 24) {
                    byte[] sprite = extractSprite(pixels, sx, sy);
                    if (notEmpty(sprite)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.format("; %d (%d,%d)\n", nr, sx, sy));
                        appendSprite(sb, sprite);
                        System.out.print(sb);
                        nr++;
                    }
                }
            }
        }
    }

    private boolean notEmpty(byte[] sprite) {
        byte bits = 0;
        for (int i = 0; i < 64; i++) {
            bits |= sprite[i];
        }
        return bits != 0;
    }

    private void appendSprite(StringBuilder sb, byte[] sprite) {
        boolean first = true;
        for (int i = 0; i < 64; i++) {
            if (first) {
                sb.append(PREFIX);
                sb.append(" ");
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(String.format("$%1$02x", sprite[i]));
            if (i == 20 || i == 41 || i == 62 || i == 63) {
                sb.append("\n");
                first = true;
            }
        }
    }

}
