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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a virtual file abstraction, allowing for custom file implementations not backed by the physical file system.
 * <p>
 * Supports custom metadata, children, and file operations for virtualized file structures.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *     VirtualFile vfile = new VirtualFile("/virtual/path.txt", true, 1024, false, new ArrayList<>());
 *     vfile.setName("custom.txt");
 *     vfile.add(new File("/virtual/child.txt"));
 *     System.out.println(vfile.getName());
 * </pre>
 *
 * <b>Important Methods:</b>
 * <ul>
 *   <li>{@link #getName()} - Returns the virtual file name.</li>
 *   <li>{@link #add(File)} - Adds a child file to this virtual file.</li>
 *   <li>{@link #list()} - Lists child file paths.</li>
 *   <li>{@link #listFiles()} - Lists child files as File objects.</li>
 *   <li>{@link #isCanWrite()} - Checks if the file is writable.</li>
 *   <li>{@link #isDirectory()} - Checks if the file is a directory.</li>
 *   <li>{@link #delete()} - Deletes the file if writable.</li>
 * </ul>
 *
 * @author Dynamia Soluciones IT S.A.S
 * @since 1.0
 */
public class VirtualFile extends File {

    /**
     *
     */
    private static final long serialVersionUID = 4878149422782768585L;

    private boolean canWrite;
    private long length;
    private boolean directory;
    private List<File> children = new ArrayList<>();
    private String name;

    public VirtualFile(File parent, String child) {
        super(parent, child);
        this.name = super.getName();

    }

    public VirtualFile(String parent, String child) {
        super(parent, child);
        this.name = super.getName();
    }

    public VirtualFile(String pathname) {
        super(pathname);
        this.name = super.getName();
    }

    public VirtualFile(URI uri) {
        super(uri);
        this.name = super.getName();
    }

    public VirtualFile(String pathname, boolean canWrite, long length, boolean directory, List<File> children) {
        super(pathname);
        this.canWrite = canWrite;
        this.length = length;
        this.directory = directory;
        this.children = children;
        this.name = super.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a child file to this virtual file.
     *
     * @param file the child file to be added
     */
    public void add(File file) {
        children.add(file);
        directory = true;
        canWrite = true;
    }

    /**
     * Lists child file paths.
     *
     * @return an array of child file paths
     */
    @Override
    public String[] list() {
        return getChildren().stream().map(File::getPath).toArray(String[]::new);
    }

    /**
     * Lists child files as File objects.
     *
     * @return an array of child files
     */
    @Override
    public File[] listFiles() {
        return getChildren().toArray(new File[0]);
    }

    /**
     * Checks if the file is writable.
     *
     * @return true if the file is writable, false otherwise
     */
    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    /**
     * Returns the virtual file length.
     *
     * @return the length of the virtual file
     */
    @Override
    public long length() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Checks if the file is a directory.
     *
     * @return true if the file is a directory, false otherwise
     */
    @Override
    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    /**
     * Returns the list of child files.
     *
     * @return the list of child files
     */
    public List<File> getChildren() {
        return children;
    }

    /**
     * Deletes the file if writable.
     *
     * @return true if the file was deleted, false otherwise
     */
    @Override
    public boolean delete() {
        if (canWrite) {
            return super.delete();
        } else {
            return false;
        }
    }

}
