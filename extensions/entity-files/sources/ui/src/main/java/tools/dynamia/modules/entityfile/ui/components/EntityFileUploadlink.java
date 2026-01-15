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

package tools.dynamia.modules.entityfile.ui.components;

import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.FileInfo;
import tools.dynamia.modules.entityfile.EntityFileException;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.Uploadlink;

import java.io.File;
import java.io.Serial;

public class EntityFileUploadlink extends Uploadlink {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -2182747459195865750L;

    static {
        BindingComponentIndex.getInstance().put("value", EntityFileUploadlink.class);
        ComponentAliasIndex.getInstance().put("entityfileUploadlink", EntityFileUploadlink.class);
    }

    private EntityFile entityFile;
    private final EntityFileService service = Containers.get().findObject(EntityFileService.class);
    private boolean shared;
    private String subfolder;
    private String storedFileName;

    public EntityFile getValue() {
        if (entityFile == null && getUploadedFile() != null) {
            onFileUpload();
        }

        return entityFile;
    }

    public void setValue(EntityFile entityFile) {
        this.entityFile = entityFile;
        if (entityFile != null) {
            configureFileInfo();
        } else {
            setUploadedFile(null);
            setLabel(Messages.get(Uploadlink.class, "upload"));
        }
    }

    private void configureFileInfo() {
        setUploadedFile(new FileInfo(new File("")));
        setLabel(entityFile.getName());
        setClientDataAttribute("uuid", entityFile.getUuid());
    }

    @Override
    protected void onFileUpload() {

        try {
            UploadedFileInfo uploadedFileInfo = new UploadedFileInfo(getUploadedFile());
            uploadedFileInfo.setShared(isShared());
            uploadedFileInfo.setSubfolder(getSubfolder());
            if (storedFileName != null && !storedFileName.isEmpty()) {
                if (storedFileName.equals("real")) {
                    uploadedFileInfo.setStoredFileName(getUploadedFile().getName());
                } else {
                    uploadedFileInfo.setStoredFileName(storedFileName);
                }
            }
            entityFile = service.createTemporalEntityFile(uploadedFileInfo);

            setLabel(entityFile.getName());

        } catch (Exception e) {
            throw new EntityFileException(e);
        }

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

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

}
