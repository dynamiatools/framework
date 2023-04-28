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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

    public void add(File file) {
        children.add(file);
        directory = true;
        canWrite = true;
    }

    @Override
    public String[] list() {
        return getChildren().stream().map(File::getPath).toArray(String[]::new);
    }

    @Override
    public String[] list(FilenameFilter filter) {
        return getChildren().stream().filter(f -> filter.accept(f.getParentFile(), f.getName())).map(File::getPath)
                .toArray(String[]::new);
    }

    @Override
    public File[] listFiles() {
        return getChildren().toArray(new File[0]);
    }

    @Override
    public File[] listFiles(FileFilter filter) {
        assert filter != null;
        return getChildren().stream().filter(filter::accept).toArray(File[]::new);
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    @Override
    public long length() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public List<File> getChildren() {
        return children;
    }

    @Override
    public boolean delete() {
        if (canWrite) {
            return super.delete();
        } else {
            return false;
        }
    }

}
