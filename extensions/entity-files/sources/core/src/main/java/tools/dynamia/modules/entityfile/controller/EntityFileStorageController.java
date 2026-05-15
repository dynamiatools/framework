package tools.dynamia.modules.entityfile.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Controller;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.impl.SpringResource;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.EntityFileSecurityProvider;
import tools.dynamia.modules.entityfile.EntityFileStorage;
import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorage;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler;
import tools.dynamia.modules.entityfile.service.EntityFileService;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler.getParam;
import static tools.dynamia.modules.entityfile.local.LocalEntityFileStorageHandler.isThumbnail;

@Controller
public class EntityFileStorageController {

    private final EntityFileService entityFileService;

    public EntityFileStorageController(EntityFileService entityFileService) {
        this.entityFileService = entityFileService;

    }

    @GetMapping(value = "/storage/{uuid}/{file}")
    public ResponseEntity<Resource> get(@PathVariable("uuid") String uuid, @PathVariable String file, HttpServletRequest request) {
        var entityFile = entityFileService.getEntityFile(uuid);
        if (entityFile == null) {
            return ResponseEntity.notFound().build();
        }

        EntityFileAccountProvider accountProvider = Containers.get().findObject(EntityFileAccountProvider.class);
        if (accountProvider != null) {
            if (entityFile.getAccountId() != null) {
                if (!entityFile.getAccountId().equals(accountProvider.getAccountId())) {
                    return ResponseEntity.notFound().build();
                }
            }
        }

        if (!entityFile.isShared()) {
            EntityFileSecurityProvider securityProvider = Containers.get().findObject(EntityFileSecurityProvider.class);
            if (securityProvider != null && !securityProvider.canAccess(entityFile)) {
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

    private int safeSize(String value, int def) {
        try {
            return Math.min(Math.max(Integer.parseInt(value), 1), 2000);
        } catch (Exception e) {
            return def;
        }
    }
}
