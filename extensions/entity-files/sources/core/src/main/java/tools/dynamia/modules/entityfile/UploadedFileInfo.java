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

package tools.dynamia.modules.entityfile;

import tools.dynamia.io.FileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadedFileInfo {

    private String fullName;
    private String contentType;
    private InputStream inputStream;
    private EntityFile parent;
    private boolean shared;
    private String subfolder;
    private String storedFileName;
    private Long accountId;

    private long length;

    private Object source;
    private String externalRef;

    public UploadedFileInfo() {
        //default
    }

    public UploadedFileInfo(File file) {
        this(new FileInfo(file));
    }

    public UploadedFileInfo(FileInfo info) {
        this.fullName = info.getName();
        this.length = info.getFile().length();
        this.source = info.getFile();
    }

    public UploadedFileInfo(Path path) {
        this.fullName = path.getFileName().toString();
        this.source = path;
        try {
            this.length = Files.size(path);
        } catch (IOException e) {
            //ignore
        }

    }

    public UploadedFileInfo(String fullName, InputStream inputStream) {
        super();
        this.fullName = fullName;
        this.inputStream = inputStream;
        this.source = inputStream;
    }

    public UploadedFileInfo(String fullName, String contentType, InputStream inputStream) {
        super();
        this.fullName = fullName;
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.source = inputStream;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public EntityFile getParent() {
        return parent;
    }

    public void setParent(EntityFile parent) {
        this.parent = parent;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean hasInputStream() {
        return inputStream != null;
    }

    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                if (source instanceof File) {
                    inputStream = new FileInputStream((File) source);
                } else if (source instanceof Path) {
                    inputStream = Files.newInputStream((Path) source);
                }
            } catch (IOException e) {
                throw new EntityFileException(e);
            }
        }
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getSubfolder() {
        return subfolder;
    }

    public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public Object getSource() {
        return source;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }
}
