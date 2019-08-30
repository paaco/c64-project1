package main.java;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

// Use case: convert 1 of meer sprites uit de spritesheet png naar bytes (binair of asm)

public class Png2Sprites {

    private Png2Sprites(String srcfname) {
        srcfilename = srcfname;
    }

    private final String srcfilename;

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

    public static void main(String[] args) throws Exception {
        new Png2Sprites(args[0]).run();
/*      if (args.length > 0) {
            System.out.println("Reading " + args[0]);
            MCBitmap bitmap = new KoalaReader().load(args[0]);
            for (int i = 0; i < 40*8; i += 8) {
                System.out.print(bitmap.parseBits(i));
            }
            System.out.println();
        } else {
            System.out.println("Usage: KoalaReader <file.kla>");
        }*/
    }

    private void run() throws IOException {
        BufferedImage image = ImageIO.read(new File(srcfilename));
        System.out.format("image is %d x %d\n", image.getHeight(), image.getWidth());
        // TODO sprites zijn 21 pixels hoog en 24 pixels breed, dus verwacht hier een veelvoud van
        // TODO verwacht een indexcolormodel
        ColorModel cm = image.getColorModel();
        if ((cm instanceof IndexColorModel)) {
            final IndexColorModel colorModel = (IndexColorModel) cm;
            Raster pixels = image.getData();
            for (int y = 0; y < 21; y++) {
                for (int x = 24*4; x < 24*4+24; x++) {
                    int pixel = pixels.getSample(x, y, 0);
                    System.out.format("%1x ", pixel);
                }
                System.out.println();
            }
//            byte[] sprite = raster2hires(pixels, 0, 0, 0);
            byte[] sprite = raster2mc(pixels, 4*24, 0, 0, 1, 2);
            // text output: ACME style uses !byte, KickAssembler expects .byte, DreamAss uses .db
            for (int i = 0; i < 63; i += 3) {
                System.out.format("!byte $%2$02x,$%3$02x,$%4$02x ; offset %1$d\n", i, sprite[i], sprite[i+1], sprite[i+2]);
            }
        }
    }

}
