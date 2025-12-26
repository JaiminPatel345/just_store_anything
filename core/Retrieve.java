import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class Retrieve {

    static void getFrames(String inputVideoPath, String outputFilePath) throws IOException, JCodecException, FileNotFoundException {
        File file = new File(inputVideoPath);
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

        FileOutputStream fos = new FileOutputStream(outputFilePath, false);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        Picture picture = grab.getNativeFrame();
        int totalBytes = getMetadataFromFrame(picture);
        while (null != (picture = grab.getNativeFrame())) {

            frameToByteArray(picture, bos, totalBytes);

        }
        bos.flush();

    }

    static int getMetadataFromFrame(Picture picture) {
        BufferedImage frame = AWTUtil.toBufferedImage(picture);
        int totalBytes = 0;
        for (int k = 0; k < 32; k++) {
            int rgb = frame.getRGB(k, 0);
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            boolean isWhite = (red > 128) && (green > 128) && (blue > 128);
            if (isWhite) {
                totalBytes |= (1 << (k));
            }
        }
        System.out.println("Total length : " + totalBytes);
        return totalBytes;
    }

    static void frameToByteArray(Picture picture, BufferedOutputStream bos, int totalBytes) throws IOException {
        BufferedImage frame = AWTUtil.toBufferedImage(picture);
        final int height = frame.getHeight();
        final int width = frame.getWidth();
        byte[] bytes = new byte[width / 8];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j += 8) {
                byte myByte = 0;
                for (int k = 0; k < 8; k++) {
                    int rgb = frame.getRGB(j + k, i);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    boolean isWhite = (red > 128) && (green > 128) && (blue > 128);
                    if (isWhite) {
                        myByte |= (byte) (1 << (7 - k));
                    }
                }

                bytes[j / 8] = myByte;
                totalBytes--;
                if (totalBytes == 0) {
                    bos.write(Arrays.copyOfRange(bytes, 0, j / 8 + 1));
                    return;
                }
            }
            bos.write(bytes);

        }
    }

    static void main() {
        try {
            final String inputVideoPath = "outputs/abc.mp4";
            System.out.println("===== Start Retrieving ====");
            final String outputFilePath = "outputs/resume.pdf";
            final String secretKey = "abc123";
            final int width = 1920;
            final int height = 1080;

            getFrames(inputVideoPath, outputFilePath);


        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }
}
