package tools.dynamia.modules.entityfile.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Controller;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.EntityFileSecurityProvider;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.web.util.HttpUtils;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler.getParam;
import static tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler.isThumbnail;

/**
 * Web controller responsible for exporting, downloading and uploading {@link EntityFile} resources.
 * <p>
 * In addition to read/download operations, this controller exposes upload endpoints for both
 * {@code multipart/form-data} and JSON payloads containing Base64 file content. Upload requests may
 * optionally be associated with an existing domain entity using its fully qualified class name and ID.
 */
@Controller
public class EntityFileStorageController {

    private final EntityFileService entityFileService;
    private final CrudService crudService;

    /**
     * Creates a new controller instance.
     *
     * @param entityFileService service used to create, resolve and download entity files
     * @param crudService       service used to resolve target entities dynamically by class name and ID
     */
    public EntityFileStorageController(EntityFileService entityFileService, CrudService crudService) {
        this.entityFileService = entityFileService;
        this.crudService = crudService;
    }

    /**
     * Exports the main metadata of a stored file, including a public or tokenized URL when applicable.
     *
     * @param uuid     unique file identifier
     * @param response current HTTP response
     * @param request  current HTTP request
     * @return a JSON payload with basic file metadata or {@code 404} when the file cannot be resolved
     */
    @GetMapping(value = "/api/storage/{uuid}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> export(@PathVariable String uuid, HttpServletResponse response, HttpServletRequest request) {
        var entityFile = entityFileService.getEntityFile(uuid);
        if (entityFile == null) {
            return ResponseEntity.notFound().build();
        }

        if (!isSameAccount(entityFile)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(MapBuilder.put(
                        "name", entityFile.getName(),
                        "id", entityFile.getId(),
                        "uuid", entityFile.getUuid(),
                        "description", entityFile.getDescription(),
                        "size", entityFile.getSize(),
                        "version", entityFile.currentVersion(),
                        "url", buildPublicURL(entityFile)
                )
        );
    }

    /**
     * Uploads a file using {@code multipart/form-data}.
     * <p>
     * The uploaded file can optionally be linked to an existing entity by providing both
     * {@code className} and {@code entityId}. When {@code parentUuid} is provided, the new file is created
     * under the referenced directory/file tree.
     *
     * @param file           multipart file content
     * @param className      fully qualified target entity class name
     * @param entityId       target entity identifier
     * @param description    optional file description
     * @param shared         whether the file should be publicly shared
     * @param subfolder      optional logical subfolder
     * @param storedFileName optional physical stored file name override
     * @param parentUuid     optional parent entity file UUID
     * @return the created entity file metadata or an error response describing the validation failure
     */
    @PostMapping(value = "/api/storage/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadMultipart(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(required = false) String className,
                                                               @RequestParam(required = false) String entityId,
                                                               @RequestParam(required = false) String description,
                                                               @RequestParam(required = false, defaultValue = "false") boolean shared,
                                                               @RequestParam(required = false) String subfolder,
                                                               @RequestParam(required = false) String storedFileName,
                                                               @RequestParam(required = false) String parentUuid) {

        if (file == null || file.isEmpty()) {
            return error(HttpStatus.BAD_REQUEST, "File is required");
        }

        var fileName = firstText(file.getOriginalFilename(), file.getName());
        if (!hasText(fileName)) {
            return error(HttpStatus.BAD_REQUEST, "Uploaded file name is required");
        }

        try {
            UploadedFileInfo info = new UploadedFileInfo(fileName, file.getContentType(), file.getInputStream());
            info.setLength(file.getSize());
            info.setShared(shared);
            info.setSubfolder(subfolder);
            info.setStoredFileName(normalizeStoredFileName(storedFileName, fileName));
            return saveUpload(info, description, className, entityId, parentUuid);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading uploaded file: " + e.getMessage());
        }
    }

    /**
     * Uploads a file using a JSON payload that contains Base64-encoded content.
     * <p>
     * The request supports raw Base64 content as well as full data URI values. As with multipart upload,
     * the file can optionally be attached to an existing entity or nested under an existing parent UUID.
     *
     * @param request JSON request containing file metadata and Base64 content
     * @return the created entity file metadata or an error response when the payload is invalid
     */
    @PostMapping(value = "/api/storage/upload-base64", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadBase64(@RequestBody Base64UploadRequest request) {
        if (request == null) {
            return error(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        var base64Content = firstText(request.getBase64(), request.getData());
        if (!hasText(base64Content)) {
            return error(HttpStatus.BAD_REQUEST, "Base64 payload is required");
        }

        var fileName = request.getResolvedFileName();
        if (!hasText(fileName)) {
            return error(HttpStatus.BAD_REQUEST, "fileName is required");
        }

        try {
            String contentType = request.getContentType();
            String normalizedBase64 = base64Content.trim();
            if (normalizedBase64.startsWith("data:")) {
                int commaIndex = normalizedBase64.indexOf(',');
                if (commaIndex < 0) {
                    return error(HttpStatus.BAD_REQUEST, "Invalid data URI payload");
                }

                if (!hasText(contentType)) {
                    String meta = normalizedBase64.substring(5, commaIndex);
                    int separator = meta.indexOf(';');
                    contentType = separator > -1 ? meta.substring(0, separator) : meta;
                }
                normalizedBase64 = normalizedBase64.substring(commaIndex + 1);
            }

            byte[] data = Base64.getMimeDecoder().decode(normalizedBase64);
            UploadedFileInfo info = new UploadedFileInfo(fileName, contentType, new ByteArrayInputStream(data));
            info.setLength(data.length);
            info.setShared(Boolean.TRUE.equals(request.getShared()));
            info.setSubfolder(request.getSubfolder());
            info.setStoredFileName(normalizeStoredFileName(request.getStoredFileName(), fileName));
            return saveUpload(info, request.getDescription(), request.getClassName(), request.getEntityId(), request.getParentUuid());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid base64 payload");
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing base64 upload: " + e.getMessage());
        }
    }

    /**
     * Streams a stored entity file or its thumbnail representation to the client.
     *
     * @param uuid    unique file identifier
     * @param file    path placeholder containing the requested file name
     * @param request current HTTP request
     * @return the resolved resource stream or {@code 404}/{@code 403} when access is not allowed
     */
    @GetMapping(value = "/storage/{uuid}/{file}")
    public ResponseEntity<Resource> get(@PathVariable("uuid") String uuid, @PathVariable String file, HttpServletRequest request) {
        var entityFile = entityFileService.getEntityFile(uuid);
        if (entityFile == null) {
            return ResponseEntity.notFound().build();
        }

        if (!isSameAccount(entityFile)) {
            return ResponseEntity.notFound().build();
        }

        if (!entityFile.isShared()) {
            EntityFileSecurityProvider securityProvider = Containers.get().findObject(EntityFileSecurityProvider.class);

            Map<String, String> params = HttpUtils.loadParams(request);
            Map<String, String> headers = HttpUtils.loadHeaders(request);
            if (securityProvider != null && !securityProvider.canAccess(entityFile, params, headers)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }
        }

        Resource resource = null;
        var storedEntityFile = entityFile.getStoredEntityFile();
        var etag = "v" + entityFile.currentVersion();

        if (entityFile.getType() == EntityFileType.IMAGE && isThumbnail(request)) {
            String w = getParam(request, "w", "200");
            String h = getParam(request, "h", "200");
            etag += "-thumb-" + w + "x" + h;
            resource = storedEntityFile.toThumbnailResource(safeSize(w, 200), safeSize(h, 200));
        } else {
            resource = storedEntityFile.toResource();
        }


        if (resource != null && resource.exists() && resource.isReadable()) {
            var contentType = getMediaType(entityFile);

            CacheControl cacheControl;
            if (entityFile.isShared()) {
                cacheControl = CacheControl
                        .maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .immutable();
            } else {
                cacheControl = CacheControl
                        .maxAge(365, TimeUnit.DAYS)
                        .cachePrivate()
                        .immutable();
            }

            String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);

            if (etag.equals(ifNoneMatch)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_MODIFIED)
                        .eTag(etag)
                        .cacheControl(cacheControl)
                        .build();
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .cacheControl(cacheControl)
                    .header(HttpHeaders.ETAG, etag)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Persists the uploaded file after resolving optional parent and target entity references.
     *
     * @param fileInfo    uploaded file information wrapper
     * @param description optional file description
     * @param className   fully qualified target entity class name
     * @param entityId    target entity identifier
     * @param parentUuid  optional parent entity file UUID
     * @return a {@code 201 Created} response containing the created file metadata
     */
    private ResponseEntity<Map<String, Object>> saveUpload(UploadedFileInfo fileInfo, String description, String className, String entityId, String parentUuid) {
        try {
            EntityFile parent = resolveParent(parentUuid);
            Object targetEntity = resolveTargetEntity(className, entityId, parent);

            if (parent != null) {
                fileInfo.setParent(parent);
            }

            EntityFile entityFile;
            if (targetEntity != null) {
                entityFile = entityFileService.createEntityFile(fileInfo, targetEntity, description);
            } else {
                entityFile = entityFileService.createTemporalEntityFile(fileInfo);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(buildUploadResponse(entityFile));
        } catch (UploadRequestException e) {
            return error(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Resolves a parent {@link EntityFile} by UUID and validates account ownership.
     *
     * @param parentUuid optional parent UUID received from the client
     * @return the resolved parent file or {@code null} when no UUID was provided
     */
    private EntityFile resolveParent(String parentUuid) {
        if (!hasText(parentUuid)) {
            return null;
        }

        EntityFile parent = entityFileService.getEntityFile(parentUuid.trim());
        if (parent == null || !isSameAccount(parent)) {
            throw new UploadRequestException(HttpStatus.NOT_FOUND, "Parent entity file not found: " + parentUuid);
        }
        return parent;
    }

    /**
     * Resolves the target domain entity for the upload request.
     * <p>
     * The entity may come directly from {@code className/entityId} parameters or be inferred from the
     * provided parent file when it already belongs to a persistent entity.
     *
     * @param className fully qualified target entity class name
     * @param entityId  target entity identifier
     * @param parent    optional parent entity file
     * @return the resolved entity instance or {@code null} when the upload should remain temporal
     */
    private Object resolveTargetEntity(String className, String entityId, EntityFile parent) {
        boolean hasClassName = hasText(className);
        boolean hasEntityId = hasText(entityId);

        if (hasClassName != hasEntityId) {
            throw new UploadRequestException(HttpStatus.BAD_REQUEST, "className and entityId must be provided together");
        }

        Object target = null;
        if (hasClassName) {
            target = loadEntity(className.trim(), entityId.trim());
        } else if (parent != null) {
            if ("temporal".equalsIgnoreCase(parent.getTargetEntity())) {
                throw new UploadRequestException(HttpStatus.BAD_REQUEST,
                        "parentUuid belongs to a temporal entity. Please provide className and entityId");
            }
            String parentEntityId = parent.getTargetEntityId() != null ? String.valueOf(parent.getTargetEntityId()) : parent.getTargetEntitySId();
            target = loadEntity(parent.getTargetEntity(), parentEntityId);
        }

        if (parent != null && target != null && !matchesParentTarget(parent, target)) {
            throw new UploadRequestException(HttpStatus.BAD_REQUEST,
                    "Provided className/entityId does not match the parentUuid target entity");
        }

        return target;
    }

    /**
     * Loads a persistent entity by its fully qualified class name and string identifier.
     *
     * @param className fully qualified entity class name
     * @param entityId  entity identifier as received from the request
     * @return the resolved entity instance
     */
    private Object loadEntity(String className, String entityId) {
        if (!hasText(className) || !hasText(entityId)) {
            throw new UploadRequestException(HttpStatus.BAD_REQUEST, "className and entityId are required");
        }

        try {
            Class<?> entityClass = Class.forName(className);
            Object entity = findEntity(entityClass, entityId);
            if (entity == null) {
                throw new UploadRequestException(HttpStatus.NOT_FOUND,
                        "Entity not found: " + className + " with id " + entityId);
            }
            return entity;
        } catch (ClassNotFoundException e) {
            throw new UploadRequestException(HttpStatus.BAD_REQUEST, "Class not found: " + className);
        }
    }

    /**
     * Attempts to find an entity using either a numeric ID or a string-based identifier.
     *
     * @param entityClass target entity class
     * @param entityId    entity identifier in string form
     * @return the resolved entity instance, or {@code null} when no match exists
     */
    private Object findEntity(Class<?> entityClass, String entityId) {
        if (isNumeric(entityId)) {
            Object entity = crudService.find((Class) entityClass, Long.valueOf(entityId));
            if (entity != null) {
                return entity;
            }
        }

        return crudService.find((Class) entityClass, entityId);
    }

    /**
     * Verifies that the resolved target entity matches the same target metadata stored in the parent file.
     *
     * @param parent parent entity file
     * @param target resolved target domain entity
     * @return {@code true} when both point to the same entity, {@code false} otherwise
     */
    private boolean matchesParentTarget(EntityFile parent, Object target) {
        if (!Objects.equals(parent.getTargetEntity(), target.getClass().getName())) {
            return false;
        }

        Serializable targetId = DomainUtils.findEntityId(target);
        if (targetId == null) {
            return false;
        }

        if (targetId instanceof Long longId) {
            return Objects.equals(parent.getTargetEntityId(), longId);
        }

        return Objects.equals(parent.getTargetEntitySId(), targetId.toString());
    }

    /**
     * Builds the JSON response body returned after a successful upload.
     *
     * @param entityFile newly created entity file
     * @return a map containing the most relevant metadata for API clients
     */
    private Map<String, Object> buildUploadResponse(EntityFile entityFile) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", entityFile.getId());
        response.put("uuid", entityFile.getUuid());
        response.put("name", entityFile.getName());
        response.put("description", entityFile.getDescription());
        response.put("contentType", entityFile.getContentType());
        response.put("size", entityFile.getSize());
        response.put("shared", entityFile.isShared());
        response.put("subfolder", entityFile.getSubfolder());
        response.put("storedFileName", entityFile.getStoredFileName());
        response.put("targetEntity", entityFile.getTargetEntity());
        response.put("targetEntityId", entityFile.getTargetEntityId());
        response.put("targetEntitySId", entityFile.getTargetEntitySId());
        response.put("parentId", entityFile.getParent() != null ? entityFile.getParent().getId() : null);
        response.put("version", entityFile.currentVersion());
        response.put("url", entityFile.toURL());
        response.put("valid", true);
        return response;
    }

    /**
     * Builds the most appropriate public URL for an entity file.
     * <p>
     * When the file is not shared, a security token is appended if a security provider is available.
     *
     * @param entityFile entity file to expose
     * @return resolved public URL
     */
    private String buildPublicURL(EntityFile entityFile) {
        String url = entityFile.toURL();
        if (!entityFile.isShared()) {
            EntityFileSecurityProvider securityProvider = Containers.get().findObject(EntityFileSecurityProvider.class);
            if (securityProvider != null) {
                String token = securityProvider.generateToken(entityFile);
                url = entityFile.toURL() + "?token=" + token;
            }
        }
        return url;
    }

    /**
     * Creates a standardized JSON error response for upload operations.
     *
     * @param status  HTTP status to return
     * @param message human-readable error description
     * @return response entity containing the error payload
     */
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .header("X-Error-Message", message)
                .body(Map.of("error", message, "valid", false));
    }

    /**
     * Checks whether the provided text contains non-blank content.
     *
     * @param value text to evaluate
     * @return {@code true} when the value contains visible characters
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Returns the first non-blank value from the provided list.
     *
     * @param values candidate values
     * @return the first non-blank value, trimmed, or {@code null} when none is available
     */
    private String firstText(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * Resolves the final stored file name override.
     * <p>
     * The special value {@code real} means the original file name should be used.
     *
     * @param storedFileName requested stored file name
     * @param fileName       original file name
     * @return normalized stored file name or {@code null} when not provided
     */
    private String normalizeStoredFileName(String storedFileName, String fileName) {
        if (!hasText(storedFileName)) {
            return null;
        }
        if ("real".equalsIgnoreCase(storedFileName.trim())) {
            return fileName;
        }
        return storedFileName.trim();
    }

    /**
     * Determines whether the provided text can be parsed as a {@link Long} value.
     *
     * @param value text to evaluate
     * @return {@code true} when the value is numeric
     */
    private boolean isNumeric(String value) {
        if (!hasText(value)) {
            return false;
        }

        try {
            Long.parseLong(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifies whether the file belongs to the current account when account scoping is enabled.
     *
     * @param entityFile file to evaluate
     * @return {@code true} when the file can be accessed from the current account context
     */
    private boolean isSameAccount(EntityFile entityFile) {
        EntityFileAccountProvider accountProvider = Containers.get().findObject(EntityFileAccountProvider.class);
        if (accountProvider != null) {
            if (entityFile.getAccountId() != null) {
                return entityFile.getAccountId().equals(accountProvider.getAccountId());
            }
        }
        return true;
    }

    /**
     * Resolves the media type for the requested file.
     *
     * @param entityFile file whose media type should be inferred
     * @return detected media type or {@link MediaType#APPLICATION_OCTET_STREAM} as fallback
     */
    private static @NonNull MediaType getMediaType(EntityFile entityFile) {
        var contentType = MediaType.APPLICATION_OCTET_STREAM;
        try {

            if (entityFile.getContentType() != null) {
                contentType = MediaType.parseMediaType(entityFile.getContentType());
            } else {
                contentType = switch (entityFile.getExtension().toLowerCase()) {
                    case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
                    case "png" -> MediaType.IMAGE_PNG;
                    case "gif" -> MediaType.IMAGE_GIF;
                    case "pdf" -> MediaType.APPLICATION_PDF;
                    case "doc" -> MediaType.TEXT_HTML;
                    case "docx" -> MediaType.TEXT_XML;
                    case "xlsx" -> MediaType.TEXT_XML;
                    case "xls" -> MediaType.TEXT_XML;
                    case "pptx" -> MediaType.TEXT_XML;
                    case "ppt" -> MediaType.TEXT_XML;
                    case "txt" -> MediaType.TEXT_PLAIN;
                    case "html" -> MediaType.TEXT_HTML;
                    case "json" -> MediaType.APPLICATION_JSON;
                    default -> MediaType.APPLICATION_OCTET_STREAM;
                };
            }

        } catch (Exception e) {

        }
        return contentType;
    }

    /**
     * Parses and clamps thumbnail dimensions to a safe range.
     *
     * @param value requested size value
     * @param def   fallback size when parsing fails
     * @return a value between 1 and 2000
     */
    private int safeSize(String value, int def) {
        try {
            return Math.min(Math.max(Integer.parseInt(value), 1), 2000);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * JSON request payload used by the Base64 upload endpoint.
     * <p>
     * Supports both {@code fileName} and {@code name}, and both {@code base64} and {@code data}, in order
     * to simplify integration with different clients.
     */
    public static class Base64UploadRequest {

        private String fileName;
        private String name;
        private String contentType;
        private String base64;
        private String data;
        private String className;
        private String entityId;
        private String description;
        private Boolean shared;
        private String subfolder;
        private String storedFileName;
        private String parentUuid;

        /**
         * Resolves the effective file name using the first available non-blank field.
         *
         * @return resolved file name or {@code null} when neither field is present
         */
        public String getResolvedFileName() {
            if (fileName != null && !fileName.isBlank()) {
                return fileName.trim();
            }
            if (name != null && !name.isBlank()) {
                return name.trim();
            }
            return null;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getBase64() {
            return base64;
        }

        public void setBase64(String base64) {
            this.base64 = base64;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getShared() {
            return shared;
        }

        public void setShared(Boolean shared) {
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

        public String getParentUuid() {
            return parentUuid;
        }

        public void setParentUuid(String parentUuid) {
            this.parentUuid = parentUuid;
        }
    }

    /**
     * Internal exception used to abort upload processing with a controlled HTTP status code.
     */
    private static class UploadRequestException extends RuntimeException {

        private final HttpStatus status;

        /**
         * Creates a new upload request exception.
         *
         * @param status  HTTP status associated with the failure
         * @param message validation or processing error description
         */
        private UploadRequestException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }

        /**
         * Returns the HTTP status associated with this failure.
         *
         * @return failure status code
         */
        public HttpStatus getStatus() {
            return status;
        }
    }
}
