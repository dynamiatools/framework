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

package tools.dynamia.modules.entityfile.remote;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.client.RestClient;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.entityfile.EntityFileException;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;

import java.io.File;
import java.io.Serial;

/**
 * {@link EntityFileStorage} implementation that stores files in a remote
 * <a href="https://github.com/dynamiatools/simple-file-server">simple-file-server (SFS)</a> instance.
 *
 * <p>Configuration parameters (read from Spring {@link Environment} first, then from application
 * {@link Parameters}):
 * <ul>
 *   <li>{@code SFS_URL}      – base URL of the SFS server, e.g. {@code http://files.example.com:8080}</li>
 *   <li>{@code SFS_BUCKET}   – bucket name</li>
 *   <li>{@code SFS_IDENTITY} – SFS identity (used in {@code X-SFS-Identity} header)</li>
 *   <li>{@code SFS_SECRET}   – SFS secret   (used in {@code X-SFS-Secret}   header)</li>
 * </ul>
 *
 * <p>Files are served to the browser through the local proxy endpoint
 * {@link #PROXY_PATH} so that SFS credentials are never exposed to the client.</p>
 *
 * <p>The remote file key follows the same convention as {@code LocalEntityFileStorage}:
 * {@code Account{accountId}/{subfolder}/{storedFileName|uuid}}</p>
 *
 * @author Dynamia Soluciones IT
 */
@Service
public class RemoteEntityFileStorage implements EntityFileStorage {

    private final LoggingService logger = new SLF4JLoggingService(RemoteEntityFileStorage.class, "SFS: ");

    public static final String ID = "RemoteSimpleFileStorage";

    /**
     * Base path of the local proxy handler that serves SFS files to the browser.
     */
    public static final String PROXY_PATH = "/storage/remote/";

    // ── Parameter names ──────────────────────────────────────────────────────
    public static final String SFS_URL = "SFS_URL";
    public static final String SFS_BUCKET = "SFS_BUCKET";
    public static final String SFS_IDENTITY = "SFS_IDENTITY";
    public static final String SFS_SECRET = "SFS_SECRET";

    // ── Header names ─────────────────────────────────────────────────────────
    static final String HEADER_IDENTITY = "X-SFS-Identity";
    static final String HEADER_SECRET = "X-SFS-Secret";

    private final Parameters appParams;
    private final CrudService crudService;
    private final Environment environment;

    /**
     * Lazily-built RestClient; rebuilt whenever {@link #reloadParams()} is invoked.
     */
    private volatile RestClient restClient;

    public RemoteEntityFileStorage(Parameters appParams, CrudService crudService, Environment environment) {
        this.appParams = appParams;
        this.crudService = crudService;
        this.environment = environment;
    }

    // ── EntityFileStorage ────────────────────────────────────────────────────

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Remote Simple File Storage";
    }

    @Override
    public void upload(EntityFile entityFile, UploadedFileInfo fileInfo) {
        String key = buildKey(entityFile);
        String bucket = getBucket();

        logger.info("Uploading " + entityFile.getName() + " → sfs://" + bucket + "/" + key);

        try {
            // Stream the InputStream directly — InputStreamResource avoids loading
            // the entire file into memory (ResourceHttpMessageConverter streams it).
            var resource = new InputStreamResource(fileInfo.getInputStream()) {
                @Override
                public long contentLength() {
                    // Return the known length so the server gets a correct Content-Length
                    // header; -1 means unknown (chunked transfer will be used instead).
                    return fileInfo.getLength() > 0 ? fileInfo.getLength() : -1;
                }

                @Override
                public String getFilename() {
                    return fileInfo.getFullName();
                }
            };

            var response = client().put()
                    .uri(uriBuilder -> uriBuilder.replacePath("/" + bucket + "/" + key).build())
                    .header(HEADER_IDENTITY, getIdentity())
                    .header(HEADER_SECRET, getSecret())
                   // .contentType(org.springframework.http.MediaType.parseMediaType(contentType(fileInfo)))
                    .body(resource)
                    .retrieve()
                    .toBodilessEntity();

            // Use the length from fileInfo; if 0 fall back to what was set by the caller.
            if (fileInfo.getLength() > 0) {
                entityFile.setSize(fileInfo.getLength());
            }
            logger.info("Uploaded successfully [" + response.getStatusCode() + "]: " + key);
        } catch (Exception e) {
            logger.error("Error uploading entity file to SFS: " + key, e);
            throw new EntityFileException("Error uploading entity file to SFS: " + key, e);
        }
    }

    @Override
    public StoredEntityFile download(EntityFile entityFile) {
        // Return a URL that goes through the local proxy handler — SFS credentials
        // are never sent to the browser.
        String url = buildRemoteUrl(entityFile);
        return new RemoteStoredEntityFile(entityFile, url);
    }

    @Override
    public void delete(EntityFile entityFile) {
        String key = buildKey(entityFile);
        String bucket = getBucket();

        logger.info("Deleting from SFS: " + key);

        try {
            client().delete()
                    .uri(uriBuilder -> uriBuilder.replacePath("/" + bucket + "/" + key).build())
                    .header(HEADER_IDENTITY, getIdentity())
                    .header(HEADER_SECRET, getSecret())
                    .retrieve()
                    .toBodilessEntity();

            entityFile.setState(EntityFileState.DELETED);
            crudService.update(entityFile);
        } catch (Exception e) {
            throw new EntityFileException("Error deleting entity file from SFS: " + key, e);
        }
    }

    @Override
    public void reloadParams() {
        // Force recreation of the RestClient on next call so the new base URL is picked up.
        restClient = null;
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /**
     * Lazily builds (and caches) a {@link RestClient} pointed at {@link #getSfsUrl()}.
     */
    RestClient client() {
        if (restClient == null) {
            synchronized (this) {
                if (restClient == null) {
                    String base = getSfsUrl();
                    logger.info("Building RestClient for SFS base URL: " + base);
                    restClient = RestClient.builder()
                            .baseUrl(base)
                            .build();
                }
            }
        }
        return restClient;
    }

    public static String getFileName(EntityFile entityFile) {
        String subfolder = "";
        if (entityFile.getSubfolder() != null) {
            subfolder = entityFile.getSubfolder() + "/";
        }

        var name = entityFile.getName().toLowerCase().trim()
                .replace(" ", "_")
                .replace("-", "_")
                .replace("\u00F1", "n")
                .replace("\u00E1", "a")
                .replace("\u00E9", "e")
                .replace("\u00ED", "i")
                .replace("\u00F3", "o")
                .replace("\u00FA", "u");
        String storedFileName = entityFile.getUuid() + "_" + name;
        if (entityFile.getStoredFileName() != null && !entityFile.getStoredFileName().isEmpty()) {
            storedFileName = entityFile.getStoredFileName();
        }

        return subfolder + storedFileName;
    }

    public static String getAccountFolderName(Long accountId) {
        return "account" + accountId + "/";
    }

    /**
     * Builds the SFS file key using the same folder structure as {@code LocalEntityFileStorage}:
     * {@code Account{accountId}/{subfolder}/{storedFileName|uuid}}
     */
    String buildKey(EntityFile entityFile) {
        String folder = getAccountFolderName(entityFile.getAccountId());
        String fileName = getFileName(entityFile);
        return folder + fileName;
    }

    /**
     * Builds the URL of the local proxy handler that will serve the file to the browser.
     * Format: {@code /storage/remote/{uuid}/{encodedFileName}}
     */
    String buildRemoteUrl(EntityFile entityFile) {

        return getSfsUrl() + "/" + getBucket() + "/" + buildKey(entityFile);
    }

    // ── Parameter accessors ──────────────────────────────────────────────────

    String getSfsUrl() {
        return param(SFS_URL, "http://localhost:8081");
    }

    String getBucket() {
        return param(SFS_BUCKET, "files");
    }

    String getIdentity() {
        return param(SFS_IDENTITY, "");
    }

    String getSecret() {
        return param(SFS_SECRET, "");
    }

    /**
     * Reads a parameter from the Spring {@link Environment} first; falls back to
     * application {@link Parameters} (same strategy as {@code LocalEntityFileStorage}).
     */
    private String param(String name, String defaultValue) {
        String value = environment.getProperty(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        try {
            return appParams.getValue(name, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String contentType(UploadedFileInfo fileInfo) {
        return fileInfo.getContentType() != null ? fileInfo.getContentType() : "application/octet-stream";
    }

    // ── Inner class: RemoteStoredEntityFile ──────────────────────────────────

    /**
     * {@link StoredEntityFile} for files hosted on a remote SFS instance.
     * There is no local {@link File} — {@link #getRealFile()} returns {@code null}.
     * Thumbnails are generated server-side by SFS and requested through the proxy.
     */
    public static class RemoteStoredEntityFile extends StoredEntityFile {

        @Serial
        private static final long serialVersionUID = 1L;

        public RemoteStoredEntityFile(EntityFile entityFile, String removeUrl) {
            super(entityFile, removeUrl, null);
        }

        @Override
        public String getThumbnailUrl(int width, int height) {
            return getUrl() + "?w=" + width + "&h=" + height;
        }

        /**
         * No local thumbnail file available; SFS handles resizing transparently.
         */
        @Override
        public File getThumbnailFile(int width, int height) {
            return null;
        }

        @Override
        public Resource toResource() {
            try {
                return new UrlResource(getUrl());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public Resource toThumbnailResource(int width, int height) {
            try {
                return new UrlResource(getThumbnailUrl(width, height));
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}




