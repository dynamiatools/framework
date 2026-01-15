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

import tools.dynamia.commons.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents information about a file, including metadata such as name, description, size, date, extension, and directory status.
 * <p>
 * Provides utility methods for file operations and metadata management.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *     FileInfo info = new FileInfo(new File("/tmp/test.txt"));
 *     System.out.println(info.getName());
 *     System.out.println(info.getFileSize());
 *     info.addMetadata("owner", "admin");
 *     Object owner = info.getMetadata("owner");
 * </pre>
 *
 * <b>Important Methods:</b>
 * <ul>
 *   <li>{@link #getName()} - Returns the file name.</li>
 *   <li>{@link #getFile()} - Returns the underlying File object.</li>
 *   <li>{@link #getFileSize()} - Returns the formatted file size.</li>
 *   <li>{@link #getFileDate()} - Returns the last modified date.</li>
 *   <li>{@link #getExtension()} - Returns the file extension.</li>
 *   <li>{@link #isDirectory()} - Checks if the file is a directory.</li>
 *   <li>{@link #isReadOnly()} - Checks if the file is read-only.</li>
 *   <li>{@link #delete()} - Deletes the file (throws FileException if read-only).</li>
 *   <li>{@link #addMetadata(String, Object)} - Adds custom metadata.</li>
 *   <li>{@link #getMetadata(String)} - Retrieves custom metadata.</li>
 * </ul>
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 1.0
 */
public class FileInfo {

    /**
     * The name.
     */
    private String name;

    /**
     * The description.
     */
    private String description;

    /**
     * The file.
     */
    private final File file;

    /**
     * The file size.
     */
    private final String fileSize;

    /**
     * The file date.
     */
    private final Date fileDate;

    /**
     * The extension.
     */
    private final String extension;

    /**
     * The directory.
     */
    private boolean directory;

    private boolean readOnly;

    private String icon;

    private final Map<String, Object> metaData = new ConcurrentHashMap<>();

    /**
     * Instantiates a new file info.
     *
     * @param file the file
     */
    public FileInfo(File file) {
        this.file = file;
        this.name = file.getName();
        this.extension = IOUtils.getFileExtension(file);
        this.description = StringUtils
                .addSpaceBetweenWords(StringUtils.capitalize(IOUtils.getFileNameWithoutExtension(file)));
        this.fileSize = IOUtils.formatFileSize(file.length());
        this.fileDate = new Date(file.lastModified());

        this.directory = file.isDirectory();
        this.readOnly = !file.canWrite();
        this.icon = extension;

        if (isDirectory()) {
            this.icon = "folder";
        }

    }

    /**
     * Instantiates a new file info.
     *
     * @param file        the file
     * @param description the description
     */
    public FileInfo(File file, String description) {
        this(file);
        this.description = description;
    }

    /**
     * Create file info with custom name and description
     *
     */
    public FileInfo(File file, String name, String description) {
        this(file);
        this.name = name;
        this.description = description;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Checks if is directory.
     *
     * @return true, if is directory
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * Sets the directory.
     *
     * @param directory the new directory
     */
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the file date.
     *
     * @return the file date
     */
    public Date getFileDate() {
        return fileDate;
    }

    /**
     * Gets the file size.
     *
     * @return the file size
     */
    public String getFileSize() {
        return fileSize;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the extension.
     *
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    public boolean delete() {
        if (readOnly) {
            throw new FileException("File " + getName() + " is read only, cannot be deleted");
        }

        if (file != null) {
            return file.delete();
        } else {
            return false;
        }
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isImage() {
        return ImageUtil.isImage(file);
    }

    /**
     * Add a custom metadata value
     *
     */
    public void addMetadata(String name, Object value) {
        this.metaData.put(name, value);
    }

    /**
     * Get custom metadata
     *
     */
    public Object getMetadata(String name) {
        return this.metaData.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(file, fileInfo.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    @Override
    public String toString() {
        return file.getPath();
    }

    public boolean isVirtual() {
        return file instanceof VirtualFile;
    }
}
