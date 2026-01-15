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

package tools.dynamia.modules.entityfile.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.URLable;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Table(name = "mod_entity_files", indexes = {
        @Index(name = "idx_uuid", columnList = "uuid")
})
@BatchSize(size = 80)
public class EntityFile extends BaseEntity implements URLable {

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private List<EntityFile> children;
    @NotNull(message = "Enter EntityFile targetEntity name")
    private String targetEntity;
    private Long targetEntityId;
    private String targetEntitySId;
    @NotNull(message = "Enter EntityFile name")
    private String name;
    @NotNull(message = "Enter EntityFile tyoe")
    private EntityFileType type;
    private String extension = "dir";
    private String contentType;
    @ManyToOne
    @JsonIgnore
    private EntityFile parent;
    private boolean shared;
    @Column(name = "fileSize")
    private Long size = 0l;
    @Column(length = 1000)
    private String description;
    @Column(name = "fileState")
    @Enumerated(EnumType.ORDINAL)
    private EntityFileState state;
    @NotEmpty
    private String uuid = StringUtils.randomString();
    @Column(length = 1000)
    private String storageInfo;

    private String subfolder;
    private String storedFileName;

    @Column(length = 500)
    private String remoteURL;

    private Long accountId;
    private String externalRef;

    @Transient
    private boolean uploading;

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getTargetEntitySId() {
        return targetEntitySId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(String storageInfo) {
        this.storageInfo = storageInfo;
    }

    public void setTargetEntitySId(String targetEntitySId) {
        this.targetEntitySId = targetEntitySId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descripcion) {
        this.description = descripcion;
    }

    public EntityFile getParent() {
        return parent;
    }

    public void setParent(EntityFile parentDirectory) {
        if (parentDirectory != null && parentDirectory.getType() == EntityFileType.DIRECTORY) {
            this.parent = parentDirectory;
        }
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String ext) {
        this.extension = ext;
    }

    public List<EntityFile> getChildren() {
        return children;
    }

    public void setChildren(List<EntityFile> filesHijos) {
        this.children = filesHijos;
    }

    public Long getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(Long idDependencia) {
        this.targetEntityId = idDependencia;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameWithoutExtension() {
        if (name.endsWith("." + extension)) {
            return name.substring(0, name.indexOf("." + extension));
        } else {
            return name;
        }
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long sizeFile) {
        this.size = sizeFile;
    }

    public EntityFileType getType() {
        return type;
    }

    public void setType(EntityFileType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSizeAsString() {
        if (size != null) {
            return IOUtils.formatFileSize(size);
        } else {
            return null;
        }
    }

    public void setState(EntityFileState state) {
        this.state = state;
    }

    public EntityFileState getState() {
        return state;
    }

    public Long getAccountId() {
        if (accountId == null) {
            accountId = 0L;
        }
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSubfolder() {
        return subfolder;
    }

    public void setSubfolder(String subfolder) {
        this.subfolder = subfolder;
    }

    /**
     * Download this EntityFile. Internally its use
     * EntityFileService.download(this);
     *
     * @return StoredEntityFile
     */
    @JsonIgnore
    public StoredEntityFile getStoredEntityFile() {
        EntityFileService service = Containers.get().findObject(EntityFileService.class);
        if (service == null) {
            throw new NullPointerException("No EntityService was found to download Entity File");
        }
        return service.download(this);
    }

    @Override
    public String toURL() {
        if (remoteURL != null && !remoteURL.isBlank()) {
            return remoteURL;
        }
        return getStoredEntityFile().getUrl();
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public String getRemoteURL() {
        return remoteURL;
    }

    public void setRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL;
    }

    @Override
    public void url(String url) {
        setRemoteURL(url);
    }

    @Override
    public void name(String name) {
        setStoredFileName(name);
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }
}
