package com.jaimin.justStore.utils;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import java.io.*;
import java.util.Arrays;

public class RetrieveVideo {

    public static byte[] decodeVideo(InputStream inputStream) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
        grabber.start();

        try (BufferedOutputStream bos = new BufferedOutputStream(baos)) {
            Frame frame = grabber.grabImage();
            if (frame == null) {
                throw new IOException("No frames found in video");
            }

            int totalBytes = getMetadataFromFrame(frame);

            while ((frame = grabber.grabImage()) != null) {
                frameToByteArray(frame, bos, totalBytes);
            }
            bos.flush();
        } finally {
            grabber.stop();
            grabber.release();
        }

        return baos.toByteArray();
    }

    static int getMetadataFromFrame(Frame frame) {
        Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

        int totalBytes = 0;
        for (int k = 0; k < 32; k++) {
            int[] rgb = getPixelRGB(mat, k, 0);
            int red = rgb[0];
            int green = rgb[1];
            int blue = rgb[2];

            boolean isWhite = (red > 128) && (green > 128) && (blue > 128);
            if (isWhite) {
                totalBytes |= (1 << k);
            }
        }
        System.out.println("Total length : " + totalBytes);
        return totalBytes;
    }

    static void frameToByteArray(Frame frame, BufferedOutputStream bos, int totalBytes) throws IOException {
        Mat mat = new OpenCVFrameConverter.ToMat().convert(frame);

        final int height = mat.rows();
        final int width = mat.cols();
        byte[] bytes = new byte[width / 8];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j += 8) {
                byte myByte = 0;
                for (int k = 0; k < 8; k++) {
                    int[] rgb = getPixelRGB(mat, j + k, i);
                    int red = rgb[0];
                    int green = rgb[1];
                    int blue = rgb[2];

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

    private static int[] getPixelRGB(Mat mat, int x, int y) {
        byte[] data = new byte[3];
        mat.ptr(y, x).get(data);

        int blue = data[0] & 0xFF;
        int green = data[1] & 0xFF;
        int red = data[2] & 0xFF;

        return new int[]{red, green, blue};
    }
}