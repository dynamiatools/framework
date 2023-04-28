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
package tools.dynamia.zk.ui;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Toolbarbutton;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.io.FileInfo;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.ImageUtil;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.io.File;
import java.io.IOException;

public class Uploadlink extends Toolbarbutton {

    /**
     *
     */
    private static final long serialVersionUID = 3854375081463695335L;

    static {
        ComponentAliasIndex.getInstance().add(Uploadlink.class);
        BindingComponentIndex.getInstance().put("uploadedFile", Uploadlink.class);
    }

    public static String ON_FILE_UPLOADED = "onFileUploaded";
    private String uploadDirectory = System.getProperty("java.io.tmpdir") + "//" + System.currentTimeMillis();
    private int maxSize = 300;
    private String fixedFileName;
    private boolean imageOnly;
    private int imageMaxWidth;
    private int imageMaxHeight;
    private FileInfo uploadedFile;
    private String format;
    private String contentType;
    private boolean imageAutoJpg;

    public Uploadlink() {
        configureLabel();
        setClass("uploadLink");
        setUpload("true");

    }

    public void onUpload(UploadEvent event) throws Exception {
        processUploadFile(event);
    }

    private void processUploadFile(UploadEvent event) throws IOException {
        Media media = event.getMedia();
        if (imageOnly && !isImage(media.getName())) {
            UIMessages.showMessage(Messages.get(Uploadlink.class, "uploadOnlyImageAllowed"), MessageType.ERROR);
            return;
        }

        if (format != null && !media.getFormat().equals(format)) {
            UIMessages.showMessage(Messages.get(Uploadlink.class, "uploadNoFormatAllowed", format), MessageType.ERROR);
            return;
        }

        if (contentType != null && !media.getContentType().equals(contentType)) {
            UIMessages.showMessage(Messages.get(Uploadlink.class, "uploadNoContentTypeAllowed", contentType), MessageType.ERROR);
            return;
        }

        if (uploadDirectory != null) {
            String fileName = fixedFileName == null ? media.getName() : fixedFileName;
            File dir = new File(uploadDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File outputFile = new File(dir, fileName);

            if (media.isBinary()) {
                IOUtils.copy(media.getStreamData(), outputFile);
            } else {
                IOUtils.copy(media.getStringData().getBytes(), outputFile);
            }
            outputFile = processImage(fileName, dir, outputFile);
            uploadedFile = new FileInfo(outputFile);
            UIMessages.showMessage(Messages.get(Uploadlink.class, "uploadSuccess", fileName));
            configureLabel();
            onFileUpload();
            Events.postEvent(new Event(ON_FILE_UPLOADED, this, uploadedFile));
        }
    }

    private File processImage(String fileName, File dir, File outputFile) {
        if (imageOnly && imageMaxHeight > 0 && imageMaxWidth > 0) {
            int w = ImageUtil.getWidth(outputFile);
            int h = ImageUtil.getHeight(outputFile);
            if (h > imageMaxHeight || w > imageMaxWidth) {
                File newfile = new File(dir, "rzd_" + fileName);
                ImageUtil.resizeImage(outputFile, newfile, StringUtils.getFilenameExtension(fileName), imageMaxWidth, imageMaxHeight);
                outputFile = newfile;
            }
        }

        if (imageOnly && imageAutoJpg) {
            outputFile = ImageUtil.convertPngToJpg(outputFile);
        }
        return outputFile;
    }

    private boolean isImage(String name) {
        if (name == null) {
            return false;
        }
        name = name.toLowerCase();
        return (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp"));
    }

    protected void onFileUpload() {

    }

    public FileInfo getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(FileInfo uploadedFile) {
        this.uploadedFile = uploadedFile;
        configureLabel();
    }

    private void configureLabel() {
        String label = null;

        if (!isImageOnly()) {
            label = Messages.get(Uploadlink.class, "upload");
        } else {
            label = Messages.get(Uploadlink.class, "uploadImage");
        }

        setTooltiptext(label);
        if (uploadedFile != null) {
            label = uploadedFile.getName();
        }
        setLabel(label);

    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getFixedFileName() {
        return fixedFileName;
    }

    public void setFixedFileName(String fixedFileName) {
        this.fixedFileName = fixedFileName;
    }

    public boolean isImageOnly() {
        return imageOnly;
    }

    public void setImageOnly(boolean imageOnly) {
        this.imageOnly = imageOnly;
        configureLabel();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getImageMaxWidth() {
        return imageMaxWidth;
    }

    public void setImageMaxWidth(int imageMaxWidth) {
        this.imageMaxWidth = imageMaxWidth;
    }

    public int getImageMaxHeight() {
        return imageMaxHeight;
    }

    public void setImageMaxHeight(int imageMaxHeight) {
        this.imageMaxHeight = imageMaxHeight;
    }

    public boolean isImageAutoJpg() {
        return imageAutoJpg;
    }

    public void setImageAutoJpg(boolean imageAutoJpg) {
        this.imageAutoJpg = imageAutoJpg;
    }
}
