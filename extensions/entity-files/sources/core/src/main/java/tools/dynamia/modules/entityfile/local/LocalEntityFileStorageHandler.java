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

package tools.dynamia.modules.entityfile.local;

import java.io.File;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.ImageUtil;
import tools.dynamia.io.impl.SpringResource;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;

public class LocalEntityFileStorageHandler {

    private static final String UUID = "/uuid/";
    private final LocalEntityFileStorage storage;
    private final EntityFileService service;
    private EntityFileAccountProvider accountProvider;

    public LocalEntityFileStorageHandler(LocalEntityFileStorage storage, EntityFileService service) {
        this.storage = storage;
        this.service = service;
    }


    public Resource getResource(String fileName, String uuid, HttpServletRequest request) {


        if (accountProvider == null) {
            accountProvider = Containers.get().findObject(EntityFileAccountProvider.class);
            if (accountProvider == null) {
                accountProvider = () -> 0L;
            }
        }

        File file = null;
        Long currentAccountId = accountProvider.getAccountId();
        EntityFile entityFile = service.getEntityFile(uuid);



        if (entityFile != null && (currentAccountId == null || currentAccountId.equals(0L) || entityFile.isShared() || entityFile.getAccountId().equals(currentAccountId))) {

            StoredEntityFile storedEntityFile = storage.download(entityFile);

            file = storedEntityFile.getRealFile();

            if (entityFile.getType() == EntityFileType.IMAGE) {

                if (isThumbnail(request)) {
                    file = createOrLoadThumbnail(file, entityFile, request);
                }

                if (!file.exists()) {
                    SpringResource notFoundResource = (SpringResource) IOUtils.getResource("classpath:/web/tools/images/no-photo.jpg");
                    return notFoundResource.getInternalResource();
                }
            }
        }
        if (file != null) {
            return new FileSystemResource(file);
        } else {
            return null;
        }
    }

    private boolean isThumbnail(HttpServletRequest request) {
        return getParam(request, "w", null) != null && getParam(request, "h", null) != null;
    }

    private File createOrLoadThumbnail(File realImg, EntityFile entityFile, HttpServletRequest request) {

        String w = getParam(request, "w", "200");
        String h = getParam(request, "h", "200");
        String subfolder = w + "x" + h;

        File realThumbImg = new File(realImg.getParentFile(), subfolder + "/" + realImg.getName());
        if (!realThumbImg.exists()) {
            if (realImg.exists()) {
                ImageUtil.resizeImage(realImg, realThumbImg, entityFile.getExtension(), Integer.parseInt(w), Integer.parseInt(h));
            }
        }
        return realThumbImg;

    }

    public String getParam(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            value = defaultValue;
        }
        return value;
    }
}
