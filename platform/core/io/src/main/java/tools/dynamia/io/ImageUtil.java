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
 * Utility class for image manipulation and processing.
 * <p>
 * Provides methods for resizing images, converting formats, checking image types,
 * color analysis, and generating QR codes.
 * </p>
 */
public class ImageUtil {

    /**
     * File extension for JPG images.
     */
    public static final String DOT_JPG = ".jpg";
    /**
     * Format name for JPG images.
     */
    public static final String JPG = "jpg";
    /**
     * File extension for JPEG images.
     */
    public static final String DOT_JPEG = ".jpeg";

    /**
     * File extension for WEBP images.
     */
    public static final String DOT_WEBP = ".webp";
    /**
     * Format name for PNG images.
     */
    public static final String PNG = "png";
    /**
     * Format name for GIF images.
     */
    public static final String GIF = "gif";

    /**
     * Format name for WEBP images.
     */
    public static final String WEBP = "webp";

    /**
     * Resize a JPEG image to the specified width and height.
     *
     * @param input      the input JPEG file
     * @param output     the output file
     * @param thumbWidth the desired width
     * @param thumbHeight the desired height
     */
    public static void resizeJPEGImage(File input, File output, int thumbWidth, int thumbHeight) {
        resizeImage(input, output, "jpeg", thumbWidth, thumbHeight);
    }

    /**
     * Resize an image to the specified width and height.
     * See {@link ImageScaler} for custom image resize.
     *
     * @param input      the input image file
     * @param output     the output file
     * @param formatName the format name (e.g., "jpg", "png")
     * @param thumbWidth the desired width
     * @param thumbHeight the desired height
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
     * Checks if the given file is an image based on its extension or MIME type.
     *
     * @param file the file to check
     * @return true if the file is an image, false otherwise
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
     * Gets the MIME type of the given file based on its name.
     *
     * @param file the file
     * @return the MIME type as a String
     */
    public static String getMimetype(File file) {

        return URLConnection.guessContentTypeFromName(file.getName());
    }

    /**
     * Calculates the brightness of a color string.
     *
     * @param colorStr the color string (e.g., "#FFFFFF" or "RED")
     * @return the brightness value, or -1 if invalid
     */
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

    /**
     * Converts a color string to a {@link Color} object.
     *
     * @param colorStr the color string (e.g., "#FFFFFF" or "RED")
     * @return the {@link Color} object, or null if invalid
     */
    public static Color getColor(String colorStr) {

        if (colorStr == null) {
            return null;
        }

        if (colorStr.startsWith("#")) {
            return Color.decode(colorStr);
        } else {
            try {
                return (Color) BeanUtils.getField(Color.class, colorStr).get(null);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Determines if a color is considered dark.
     *
     * @param color the color string
     * @return true if the color is dark, false otherwise
     */
    public static boolean isDark(String color) {
        return getBrightness(color) < 130;
    }

    /**
     * Determines if a color is considered light.
     *
     * @param color the color string
     * @return true if the color is light, false otherwise
     */
    public static boolean isLight(String color) {
        return getBrightness(color) >= 130;
    }

    /**
     * Converts a PNG file to a JPG file.
     *
     * @param input the input PNG file
     * @return the output JPG file
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

    /**
     * Gets the width of an image file.
     *
     * @param image the image file
     * @return the width, or -1 if unable to read
     */
    public static int getWidth(File image) {
        try {
            return ImageIO.read(image).getWidth();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Gets the height of an image file.
     *
     * @param image the image file
     * @return the height, or -1 if unable to read
     */
    public static int getHeight(File image) {
        try {
            return ImageIO.read(image).getHeight();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Generates a QR code as a JPG image in a temporary file.
     *
     * @param content the content to encode in the QR code
     * @return the temporary file containing the QR code image
     */
    public static File generateQR(String content) {
        return QRCode.from(content).to(ImageType.JPG).file();
    }

    /**
     * Generates a QR code as a JPG image in a temporary file with specific width and height.
     *
     * @param content the content to encode in the QR code
     * @param width   the width of the QR code image
     * @param height  the height of the QR code image
     * @return the temporary file containing the QR code image
     */
    public static File generateQR(String content, int width, int height) {
        return QRCode.from(content).withSize(width, height).to(ImageType.JPG).file();
    }

    /**
     * Generates a QR code as a Base64-encoded string.
     *
     * @param content the content to encode in the QR code
     * @return the Base64-encoded QR code image
     */
    public static String generateQRBase64(String content) {
        try {
            return IOUtils.encodeBase64(generateQR(content));
        } catch (IOException e) {
            throw new QRGenerationException("Exception generating base 64 qr code", e);
        }
    }

    /**
     * Generates a QR code as a Base64-encoded string with specific width and height.
     *
     * @param content the content to encode in the QR code
     * @param width   the width of the QR code image
     * @param height  the height of the QR code image
     * @return the Base64-encoded QR code image
     */
    public static String generateQRBase64(String content, int width, int height) {
        try {
            return IOUtils.encodeBase64(generateQR(content, width, height));
        } catch (IOException e) {
            throw new QRGenerationException("Exception generating base 64 qr code", e);
        }
    }


    /**
     * Private constructor to prevent instantiation.
     */
    private ImageUtil() {
    }

}
