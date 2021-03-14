/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

package tools.dynamia.io.qr;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import tools.dynamia.io.ImageType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class QRCode extends AbstractQRCode {

    public static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

    protected final String text;
    protected MatrixToImageConfig matrixToImageConfig = DEFAULT_CONFIG;

    protected QRCode(String text) {
        this.text = text;
        qrWriter = new QRCodeWriter();
    }

    /**
     * Create a QR code from the given text.    <br><br>
     * <p>
     * There is a size limitation to how much you can put into a QR code. This has been tested to work with up to a length of
     * 2950
     * characters.<br><br>
     * </p>
     * <p>
     * The QRCode will have the following defaults:     <br> {size: 100x100}<br>{imageType:PNG}  <br><br>
     * </p>
     * Both size and imageType can be overridden:   <br> Image type override is done by calling {@link
     * QRCode#to(ImageType)} e.g. QRCode.from("hello world").to(JPG) <br> Size override is done
     * by calling
     * {@link QRCode#withSize} e.g. QRCode.from("hello world").to(JPG).withSize(125, 125)  <br>
     *
     * @param text the text to encode to a new QRCode, this may fail if the text is too large. <br>
     * @return the QRCode object    <br>
     */
    public static QRCode from(String text) {
        return new QRCode(text);
    }


    /**
     * Overrides the imageType from its default {@link  ImageType#PNG}
     *
     * @param imageType the {@link ImageType} you would like the resulting QR to be
     * @return the current QRCode object
     */
    public QRCode to(ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * Overrides the size of the qr from its default 125x125
     *
     * @param width  the width in pixels
     * @param height the height in pixels
     * @return the current QRCode object
     */
    public QRCode withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * Overrides the default charset by supplying a {@link EncodeHintType#CHARACTER_SET} hint to {@link
     * QRCodeWriter#encode}
     *
     * @param charset the charset as string, e.g. UTF-8
     * @return the current QRCode object
     */
    public QRCode withCharset(String charset) {
        return withHint(EncodeHintType.CHARACTER_SET, charset);
    }

    /**
     * Overrides the default error correction by supplying a {@link EncodeHintType#ERROR_CORRECTION} hint to
     * {@link QRCodeWriter#encode}
     *
     * @param level the error correction level to use by {@link QRCodeWriter#encode}
     * @return the current QRCode object
     */
    public QRCode withErrorCorrection(ErrorCorrectionLevel level) {
        return withHint(EncodeHintType.ERROR_CORRECTION, level);
    }

    /**
     * Sets hint to {@link QRCodeWriter#encode}
     *
     * @param hintType the hintType to set
     * @param value    the concrete value to set
     * @return the current QRCode object
     */
    public QRCode withHint(EncodeHintType hintType, Object value) {
        hints.put(hintType, value);
        return this;
    }

    @Override
    public File file() {
        File file;
        try {
            file = createTempFile();
            MatrixToImageWriter.writeToPath(createMatrix(text), imageType.toString(), file.toPath(), matrixToImageConfig);
        } catch (Exception e) {
            throw new QRGenerationException("Failed to create QR image from text due to underlying exception", e);
        }

        return file;
    }

    @Override
    public File file(String name) {
        File file;
        try {
            file = createTempFile(name);
            MatrixToImageWriter.writeToPath(createMatrix(text), imageType.toString(), file.toPath(), matrixToImageConfig);
        } catch (Exception e) {
            throw new QRGenerationException("Failed to create QR image from text due to underlying exception", e);
        }

        return file;
    }

    @Override
    protected void writeToStream(OutputStream stream) throws IOException, WriterException {
        MatrixToImageWriter.writeToStream(createMatrix(text), imageType.toString(), stream, matrixToImageConfig);
    }


    private File createTempSvgFile() throws IOException {
        return createTempSvgFile("QRCode");
    }

    private File createTempSvgFile(String name) throws IOException {
        File file = File.createTempFile(name, ".svg");
        file.deleteOnExit();
        return file;
    }

    public QRCode withColor(int onColor, int offColor) {
        matrixToImageConfig = new MatrixToImageConfig(onColor, offColor);
        return this;
    }
}
