/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.io;


import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.io.qr.QRCode;
import tools.dynamia.io.qr.QRGenerationException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;


/**
 * The Class ImageUtil.
 */
public class ImageUtil {

    public static final String DOT_JPG = ".jpg";
    public static final String JPG = "jpg";
    public static final String DOT_JPEG = ".jpeg";

    public static final String DOT_WEBP = ".webp";
    public static final String PNG = "png";
    public static final String GIF = "gif";

    public static final String WEBP = "webp";

    /**
     * Resize jpeg image.
     *
     * @param input       the input
     * @param output      the output
     * @param thumbWidth  the thumb width
     * @param thumbHeight the thumb height
     */
    public static void resizeJPEGImage(File input, File output, int thumbWidth, int thumbHeight) {
        resizeImage(input, output, "jpeg", thumbWidth, thumbHeight);
    }

    /**
     * Resize image. See {@link ImageScaler} for custom image resize
     *
     * @param input       the input
     * @param output      the output
     * @param formatName  the format name
     * @param thumbWidth  the thumb width
     * @param thumbHeight the thumb height
     */
    public static void resizeImage(File input, File output, String formatName, int thumbWidth, int thumbHeight) {
        try {

            BufferedImage image = ImageIO.read(input);
            BufferedImage newImage = ImageScaler.resize(image, thumbWidth, thumbHeight);
            output.getParentFile().mkdirs();
            output.createNewFile();
            ImageIO.write(newImage, formatName, output);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    /**
     * Checks if is image.
     *
     * @param file the file
     * @return true, if is image
     */
    public static boolean isImage(File file) {
        if (!file.exists()) {
            var name = file.getName().toLowerCase();
            return name.endsWith(JPG) || name.endsWith(PNG) || name.endsWith(GIF) || name.endsWith(WEBP);
        }

        String mimetype = getMimetype(file);
        if (mimetype != null) {
            return mimetype.contains("image");
        } else {
            return false;
        }
    }

    /**
     * Gets the mimetype.
     *
     * @param file the file
     * @return the mimetype
     */
    public static String getMimetype(File file) {

        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public static int getBrightness(String colorStr) {
        Color c = getColor(colorStr);
        if (c != null) {
            return (int) Math.sqrt(
                    c.getRed() * c.getRed() * .241
                            + c.getGreen() * c.getGreen() * .691
                            + c.getBlue() * c.getBlue() * .068);
        } else {
            return -1;
        }
    }

    public static Color getColor(String colorStr) {

        if (colorStr == null) {
            return null;
        }

        if (colorStr.startsWith("#")) {
            return Color.decode(colorStr);
        } else {
            try {
                return (Color) BeanUtils.getField(Color.class, colorStr).get(null);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean isDark(String color) {
        return getBrightness(color) < 130;
    }

    public static boolean isLight(String color) {
        return getBrightness(color) >= 130;
    }

    /**
     * Convert a png file to jpg
     *
     * @param input
     * @return
     */
    public static File convertPngToJpg(File input) {
        try {
            if (input.getName().endsWith(DOT_JPG) || input.getName().endsWith(DOT_JPEG)) {
                return input;
            }
            File output = new File(input.getParentFile(), StringUtils.removeFilenameExtension(input.getName()) + DOT_JPG);
            BufferedImage image = ImageIO.read(input);
            BufferedImage result = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            result.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            ImageIO.write(result, JPG, output);
            return output;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getWidth(File image) {
        try {
            return ImageIO.read(image).getWidth();
        } catch (IOException e) {
            return -1;
        }
    }

    public static int getHeight(File image) {
        try {
            return ImageIO.read(image).getHeight();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Generate QR code jpg image in a temporal file
     *
     * @param content
     * @return
     */
    public static File generateQR(String content) {
        return QRCode.from(content).to(ImageType.JPG).file();
    }

    /**
     * Generate QR code jpg image in a temporal file with specific width and height
     *
     * @param content
     * @param width
     * @param height
     * @return
     */
    public static File generateQR(String content, int width, int height) {
        return QRCode.from(content).withSize(width, height).to(ImageType.JPG).file();
    }

    /**
     * Generate qr code in base 64
     *
     * @param content
     * @return
     */
    public static String generateQRBase64(String content) {
        try {
            return IOUtils.encodeBase64(generateQR(content));
        } catch (IOException e) {
            throw new QRGenerationException("Exception generating base 64 qr code", e);
        }
    }

    /**
     * Generate qr code in base 64 with specific width and height
     *
     * @param content
     * @return
     */
    public static String generateQRBase64(String content, int width, int height) {
        try {
            return IOUtils.encodeBase64(generateQR(content, width, height));
        } catch (IOException e) {
            throw new QRGenerationException("Exception generating base 64 qr code", e);
        }
    }


    private ImageUtil() {
    }

}
