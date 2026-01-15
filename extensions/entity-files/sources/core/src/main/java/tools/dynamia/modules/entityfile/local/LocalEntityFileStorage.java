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

import org.springframework.core.env.Environment;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.io.IOUtils;
import tools.dynamia.modules.entityfile.EntityFileException;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.web.util.HttpUtils;

import java.io.File;
import java.io.IOException;

@Service
public class LocalEntityFileStorage implements EntityFileStorage {

    private final LoggingService logger = new SLF4JLoggingService(LocalEntityFileStorage.class, "Local: ");

    public static final String ID = "LocalStorage";
    private static final String LOCAL_FILES_LOCATION = "LOCAL_FILES_LOCATION";
    private static final String LOCAL_USE_HTTPS = "LOCAL_USE_HTTPS";
    private static final String LOCAL_CONTEXT_PATH = "LOCAL_CONTEXT_PATH";
    private static final String DEFAULT_LOCATION = System.getProperty("user.home") + "/localentityfiles";
    static final String LOCAL_FILE_HANDLER = "/storage/";

    private final Parameters appParams;

    private final CrudService crudService;

    private final Environment environment;

    public LocalEntityFileStorage(Parameters appParams, CrudService crudService, Environment environment) {
        this.appParams = appParams;
        this.crudService = crudService;
        this.environment = environment;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Local File Storage";
    }

    @Override
    public void upload(EntityFile entityFile, UploadedFileInfo fileInfo) {
        File realFile = getRealFile(entityFile);

        try {

            IOUtils.copy(fileInfo.getInputStream(), realFile);
            entityFile.setSize(realFile.length());
            logger.info("Uploaded to server: " + realFile);
        } catch (IOException e) {
            logger.error("Error upload local file " + realFile, e);
            throw new EntityFileException("Error upload local file " + realFile, e);
        }

    }

    @Override
    public StoredEntityFile download(EntityFile entityFile) {
        String url = generateURL(entityFile);
        StoredEntityFile sef = new LocalStoredEntityFile(entityFile, url, getRealFile(entityFile));
        return sef;
    }

    private String generateURL(EntityFile entityFile) {

        String serverPath = HttpUtils.getServerPath();
        boolean useHttps = isUseHttps();

        if (useHttps && serverPath.startsWith("http:")) {
            serverPath = serverPath.replace("http:", "https:");
        }

        String context = getContextPath();


        if (context == null) {
            context = "";
        }

        String fileName = entityFile.getName();
        fileName = fileName.replace(" ", "%20");
        String url = serverPath + context + LOCAL_FILE_HANDLER + fileName + "?uuid=" + entityFile.getUuid();

        return url;
    }

    private String getContextPath() {
        String context = System.getProperty(LOCAL_CONTEXT_PATH);
        try {
            if (context == null) {
                context = appParams.getValue(LOCAL_CONTEXT_PATH, "");
            }
        } catch (Exception e) {
            context = "";
        }
        return context;
    }

    private boolean isUseHttps() {
        boolean useHttps = false;

        String useHttpsProperty = environment.getProperty(LOCAL_USE_HTTPS);
        if (useHttpsProperty != null && (useHttpsProperty.equals("true") || useHttpsProperty.equals("false"))) {
            useHttps = Boolean.parseBoolean(useHttpsProperty);
        } else {
            try {
                useHttps = Boolean.parseBoolean(appParams.getValue(LOCAL_USE_HTTPS, "false"));
            } catch (Exception e) {
                //ignore
            }
        }
        return useHttps;
    }

    public File getParentDir() {

        String path = environment.getProperty(LOCAL_FILES_LOCATION);
        if (path == null || path.isEmpty()) {
            path = appParams.getValue(LOCAL_FILES_LOCATION, DEFAULT_LOCATION);
        }
        return new File(path);
    }

    private File getRealFile(EntityFile entityFile) {
        String subfolder = "";
        if (entityFile.getSubfolder() != null) {
            subfolder = entityFile.getSubfolder() + "/";
        }

        String storedFileName = entityFile.getUuid();
        if (entityFile.getStoredFileName() != null && !entityFile.getStoredFileName().isEmpty()) {
            storedFileName = entityFile.getStoredFileName();
        }

        String filePath = "Account" + entityFile.getAccountId() + "/" + subfolder + storedFileName;
        File parentDir = getParentDir();
        File realFile = new File(parentDir, filePath);
        try {

            if (!realFile.getParentFile().exists()) {
                realFile.getParentFile().mkdirs();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return realFile;
    }

    @Override
    public void delete(EntityFile entityFile) {
        try {
            File realFile = getRealFile(entityFile);
            if (realFile != null && realFile.exists()) {
                realFile.delete();
            }
            entityFile.setState(EntityFileState.DELETED);
            crudService.update(entityFile);
        } catch (Exception e) {
            throw new EntityFileException("Error deleting entity file " + entityFile, e);
        }
    }

    private class LocalStoredEntityFile extends StoredEntityFile {

        /**
         *
         */
        private static final long serialVersionUID = -6295813096900514353L;

        public LocalStoredEntityFile(EntityFile entityFile, String url, File realFile) {
            super(entityFile, url, realFile);
            // TODO Auto-generated constructor stub
        }

        @Override
        public String getThumbnailUrl(int width, int height) {
            return getUrl() + "&w=" + width + "&h=" + height;
        }

    }

    public void copy(EntityFile entityFile, EntityFileStorage otherStorage) {
        if (otherStorage instanceof LocalEntityFileStorage) {
            throw new ValidationError("Cannot move to Local Storage");
        }

        if (ID.equals(entityFile.getStorageInfo())) {
            try {
                var localFile = getRealFile(entityFile);
                if (localFile.exists()) {
                    var uploadInfo = new UploadedFileInfo(localFile);
                    uploadInfo.setStoredFileName(entityFile.getStoredFileName());
                    otherStorage.upload(entityFile, uploadInfo);
                } else {
                    throw new EntityFileException("EntityFile local file " + localFile.getName() + " dont exits");
                }
            } catch (Exception e) {
                throw new EntityFileException("Error moving entity file " + entityFile.getName() + " (" + entityFile.getUuid() + ") to new storage " + otherStorage, e);
            }
        }

    }

    @Override
    public String toString() {
        return getName();
    }
}
