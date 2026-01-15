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

package tools.dynamia.modules.entityfiles.s3;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.ApplicationParameters;
import tools.dynamia.io.ImageUtil;
import tools.dynamia.modules.entityfile.EntityFileException;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link EntityFileStorage} implementation that store files in Amazon S3 service.
 * The following environment variables are required AWS_ACCESS_KEY_ID, AWS_SECRET_KEY, AWS_S3_ENDPOINT, AWS_S3_BUCKET. The
 * values are loaded using Spring {@link Environment} service
 */
@Service
public class S3EntityFileStorage implements EntityFileStorage {

    public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";
    public static final String AWS_S3_ENDPOINT = "AWS_S3_ENDPOINT";
    public static final String AWS_S3_REGION = "AWS_S3_REGION";
    public static final String AWS_S3_BUCKET = "AWS_S3_BUCKET";
    private static final Logger log = LoggerFactory.getLogger(S3EntityFileStorage.class);
    public static final int PRESIGNED_URL_TIMEOUT = 30;
    private final LoggingService logger = new SLF4JLoggingService(S3EntityFileStorage.class, "S3: ");

    private final SimpleCache<String, String> URL_CACHE = new SimpleCache<>();
    private final SimpleCache<String, String> PARAMS_CACHE = new SimpleCache<>();

    public static final String ID = "AWSS3Storage";
    private S3AsyncClient s3Client;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private Environment environment;


    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "AWS S3 Storage";
    }

    /**
     * Upload entity file to S3 bucket
     *
     * @param entityFile the entity file
     * @param fileInfo   the uploade file info
     * @return the S3 client
     */
    @Override
    public void upload(EntityFile entityFile, UploadedFileInfo fileInfo) {
        try {

            String folder = getAccountFolderName(entityFile.getAccountId());
            String fileName = getFileName(entityFile);

            // Metadata

            File fileToUpload;
            long sourceLength = 0;
            long length = fileInfo.getLength();


            if (fileInfo.getSource() instanceof File file) {
                sourceLength = file.length();
                fileToUpload = file;
            } else if (fileInfo.getSource() instanceof Path path) {
                sourceLength = path.toFile().length();
                fileToUpload = path.toFile();
            } else {
                fileToUpload = null;
            }

            if (length <= 0 && sourceLength > 0) {
                length = sourceLength;
            }


            final var metadata = Map.of(
                    "accountId", entityFile.getAccountId() != null ? entityFile.getAccountId().toString() : "",
                    "uuid", entityFile.getUuid(),
                    "creator", entityFile.getCreator() != null ? entityFile.getCreator() : "anonymous",
                    "databaseId", entityFile.getId() != null ? String.valueOf(entityFile.getId()) : ""
            );

            final var contentType = URLConnection.guessContentTypeFromName(entityFile.getName());
            final var key = folder + fileName;
            final var bucket = getBucketName();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .metadata(metadata)
                    .contentLength(length)
                    .contentType(contentType)
                    .acl(entityFile.isShared() ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                    .build();


            AsyncRequestBody body = null;
            if (fileToUpload != null && fileToUpload.exists()) {
                logger.info("Uploading file " + fileToUpload.getPath() + " to " + key);
                body = AsyncRequestBody.fromFile(fileToUpload);
            } else if (fileInfo.hasInputStream()) {
                logger.info("Uploading input stream from " + fileInfo.getFullName() + " to " + key);
                body = AsyncRequestBody.fromInputStream(fileInfo.getInputStream(), length, executorService);
            }
            entityFile.setUploading(true);
            getClient().putObject(request, body)
                    .whenComplete((response, throwable) -> {
                        if (throwable != null) {
                            logger.error("Error uploading entity file " + entityFile.getName() + " to S3", throwable);
                            throw new EntityFileException("Error uploading file " + entityFile.getName(), throwable);
                        } else {
                            logger.info("Entity file " + entityFile.getName() + " uploaded");
                        }

                        if (fileToUpload != null && fileToUpload.delete()) {
                            logger.info("Deleted temporal file: " + fileToUpload);
                        }
                        entityFile.setUploading(false);
                    });


        } catch (Exception e) {
            logger.error("Error sending PUT request for entity file " + entityFile.getName() + " to S3", e);
            throw new EntityFileException("Error sending PUT request fo file " + entityFile.getName(), e);
        }
    }

    @Override
    public StoredEntityFile download(EntityFile entityFile) {
        String urlKey = entityFile.getUuid();
        String url = URL_CACHE.get(urlKey);
        String fileName = getFileName(entityFile);

        if (url == null) {

            String folder = getAccountFolderName(entityFile.getAccountId());
            if (entityFile.isShared()) {
                url = generateStaticURL(getBucketName(), folder + fileName);
                URL_CACHE.add(urlKey, url);
            } else {
                url = generateSignedURL(getBucketName(), folder + fileName);
            }
        }

        return new S3StoredEntityFile(entityFile, url, new File(fileName));
    }

    protected String generateSignedURL(String bucketName, String fileName) {


        PresignedGetObjectRequest presignedRequest = S3Utils.generatePresignedObjetRequest(bucketName, fileName, Duration.ofMinutes(PRESIGNED_URL_TIMEOUT)
                , getAccessKey(), getSecretKey(), getRegion());
        logger.info("Presigned URL: " + presignedRequest.url().toString());
        logger.info("HTTP method: " + presignedRequest.httpRequest().method());

        return presignedRequest.url().toExternalForm();

    }

    private String generateStaticURL(String bucketName, String fileName) {
        return S3Utils.generateStaticURL(bucketName, fileName, getRegion());
    }


    private String getFileName(EntityFile entityFile) {
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

    /**
     * Get or build a S3 async client using static credentials
     *
     * @return S3 Async client
     */
    protected S3AsyncClient getClient() {

        if (s3Client == null) {
            s3Client = S3Utils.buildS3AsyncClient(getAccessKey(), getSecretKey(), getRegion())
                    .build();
        }
        return s3Client;


    }

    protected String getAccountFolderName(Long accountId) {
        return "account" + accountId + "/";
    }

    /**
     * Generate thumbnail url
     */
    protected String generateThumbnailURL(EntityFile entityFile, int w, int h) {
        if (entityFile.getType() == EntityFileType.IMAGE || EntityFileType.getFileType(entityFile.getExtension()) == EntityFileType.IMAGE) {
            if (entityFile.isUploading()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }

            String urlKey = entityFile.getUuid() + w + "x" + h;
            String url = URL_CACHE.get(urlKey);
            if (url == null) {
                String bucketName = getBucketName();
                String folder = getAccountFolderName(entityFile.getAccountId());
                String fileName = getFileName(entityFile);
                String thumbfileName = w + "x" + h + "/" + fileName;

                if (!objectExists(bucketName, folder + thumbfileName)) {
                    url = createAndUploadThumbnail(entityFile, bucketName, folder, fileName, thumbfileName, w, h);
                }
                if (url != null) {
                    URL_CACHE.add(urlKey, url);
                }
            }
            return url;
        } else {
            return "#";
        }
    }

    /**
     * Create and upload thumbnail
     */
    protected String createAndUploadThumbnail(EntityFile entityFile, String bucketName, String folder, String fileName, String thumbfileName,
                                              int w, int h) {
        try {

            File localDestination = File.createTempFile(System.currentTimeMillis() + "file", entityFile.getName());
            File localThumbDestination = File.createTempFile(System.currentTimeMillis() + "thumb", entityFile.getName());
            var url = download(entityFile).getUrl();
            Files.copy(new URL(url).openStream(), localDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ImageUtil.resizeImage(localDestination, localThumbDestination, entityFile.getExtension(), w, h);

            // metadata
            var metadata = Map.of(
                    "thumbnail", "true",
                    "uuid", entityFile.getUuid(),
                    "width", String.valueOf(w),
                    "height", String.valueOf(h));

            String key = folder + thumbfileName;
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .metadata(metadata)
                    .contentLength(localThumbDestination.length())
                    .contentType("image/" + entityFile.getExtension())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();


            var future = getClient().putObject(request, AsyncRequestBody.fromFile(localThumbDestination));
            var response = future.get();
            localThumbDestination.delete();

            return generateStaticURL(bucketName, key);
        } catch (Exception e) {
            logger.error("Error creating thumbnail for " + entityFile.getName() + "  " + w + "x" + h + "  " + fileName, e);
            return null;
        }
    }

    public boolean objectExists(String bucketName, String key) {
        return S3Utils.objectExists(getClient(), bucketName, key);
    }


    @Override
    public void delete(EntityFile entityFile) {
        String folder = getAccountFolderName(entityFile.getAccountId());
        String key = folder + getFileName(entityFile);

        S3Utils.deleteFile(getClient(), getBucketName(), key)
                .whenComplete((deleteObjectResponse, throwable) -> {
                    if (throwable != null) {
                        logger.error("Error deleting entity file " + entityFile.getName() + " from S3", throwable);
                    } else {
                        logger.info("Entity file " + entityFile.getName() + " deleted from S3");
                    }
                });
    }


    public String getBucketName() {
        return getParameter(AWS_S3_BUCKET);
    }

    public String getEndpoint() {
        return getParameter(AWS_S3_ENDPOINT);
    }

    public String getAccessKey() {
        return getParameter(AWS_ACCESS_KEY_ID);
    }

    public String getRegion() {
        var region = getParameter(AWS_S3_REGION);
        if (region == null || region.isBlank()) {
            region = "us-east-1";
        }
        return region;
    }


    protected String getParameter(String name) {
        return PARAMS_CACHE.getOrLoad(name, s -> {
            var value = ApplicationParameters.get().getValue(name);
            if (value == null) {
                value = environment.getProperty(name);
            }
            return value;
        });
    }

    public String getSecretKey() {
        return getParameter(AWS_SECRET_KEY);
    }

    @Override
    public void reloadParams() {
        PARAMS_CACHE.clear();
        URL_CACHE.clear();
        s3Client = null;
    }

    class S3StoredEntityFile extends StoredEntityFile {

        public S3StoredEntityFile(EntityFile entityFile, String url, File realFile) {
            super(entityFile, url, realFile);
        }

        @Override
        public String getThumbnailUrl(int width, int height) {
            return generateThumbnailURL(getEntityFile(), width, width);
        }
    }


    @Override
    public String toString() {
        return getName();
    }
}
