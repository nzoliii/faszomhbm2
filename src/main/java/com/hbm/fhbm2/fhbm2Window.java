package com.hbm.fhbm2;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.Display;

public class fhbm2Window {

    /**
     * Sets the window title and icon using PNG files located in the classpath.
     *
     * @param title       The window title text.
     * @param iconPath16  Path to 16x16 PNG icon.
     * @param iconPath32  Path to 32x32 PNG icon.
     */

    public static void apply(String title, String iconPath16, String iconPath32) {
        try {
            Display.setTitle(title);

            List<ByteBuffer> icons = new ArrayList<>();

            icons.add(loadIcon(iconPath16));
            icons.add(loadIcon(iconPath32));

            Display.setIcon(icons.toArray(new ByteBuffer[0]));
        } catch (Exception e) {
            System.err.println("[fhbm2Window] Failed to set window title/icon:");
            e.printStackTrace();
        }
    }

    private static ByteBuffer loadIcon(String path) throws Exception {
        try (InputStream is = fhbm2Window.class.getResourceAsStream(path)) {
            if (is == null)
                throw new IllegalArgumentException("Icon not found at: " + path);

            BufferedImage image = ImageIO.read(is);
            return convertToByteBuffer(image);
        }
    }

    private static ByteBuffer convertToByteBuffer(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }
}