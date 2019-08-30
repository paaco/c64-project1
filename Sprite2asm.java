import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

// Convert spritesheet to asm

// TODO: use raster2mc when filename contains used colors (eg ".mc####")

public class Sprite2asm {

    // text output: ACME style uses !byte, KickAssembler expects .byte, DreamAss uses .db
    private static final String PREFIX = "!byte";

    private Sprite2asm(String fname) {
        srcfilename = fname;
    }

    private final String srcfilename;

    public static void main(String[] args) throws Exception {
        new Sprite2asm(args[0]).run();
    }

    // bgIndex is '0', any other color is '1' (sprite color)
    private byte[] raster2hires(Raster pixels, int xoff, int yoff, int bgIndex) {
        byte[] buf = new byte[64];
        int bufoffset = 0;
        int bitcount = 0;
        byte b = 0;
        for (int y = 0; y < 21; y++) {
            for (int x = 0; x < 24; x++) {
                int pixel = pixels.getSample(x + xoff, y + yoff, 0);
                b <<= 1;
                if (pixel != bgIndex) b |= 1;
                // flush
                bitcount++;
                if (bitcount == 8) {
                    buf[bufoffset++] = b;
                    bitcount = 0;
                    b = 0;
                }
            }
        }
        return buf;
    }

    // bgIndex is '00'(0), mc1Index is '01'(1), mc2Index is '11'(3), any other is '10'(2) (sprite color)
    private byte[] raster2mc(Raster pixels, int xoff, int yoff, int bgIndex, int mc1Index, int mc2Index) {
        byte[] buf = new byte[64];
        int bufoffset = 0;
        int bitcount = 0;
        byte b = 0;
        for (int y = 0; y < 21; y++) {
            for (int x = 0; x < 24; x += 2) {
                int pixel = pixels.getSample(x + xoff, y + yoff, 0);
                b <<= 2;
                if (pixel == bgIndex) b |= 0;
                else if (pixel == mc1Index) b |= 1;
                else if (pixel == mc2Index) b |= 3;
                else b |= 2;
                // flush
                bitcount++;
                if (bitcount == 4) {
                    buf[bufoffset++] = b;
                    bitcount = 0;
                    b = 0;
                }
            }
        }
        return buf;
    }

    private void run() throws IOException {
        File f = new File(srcfilename);
        BufferedImage image = ImageIO.read(f);
        int height = image.getHeight();
        int width = image.getWidth();
        if (height % 21 != 0 || width % 24 != 0) {
            System.err.println("ERROR: image should be a multiple of 24x21");
        } else if (!(image.getColorModel() instanceof IndexColorModel)) {
            System.err.println("ERROR: image should have palette");
        } else {
            System.out.println("; Sprite2asm " + f.getName() + " " + DateFormat.getDateTimeInstance().format(new Date()));
            Raster pixels = image.getData();
            int nr = 0;
            for (int sy = 0; sy < height; sy += 21) {
                for (int sx = 0; sx < width; sx += 24) {
                    byte[] sprite = raster2hires(pixels, sx, sy, 0);
                    //byte[] sprite = raster2mc(pixels, sx, sy, 0, 1, 2);
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
