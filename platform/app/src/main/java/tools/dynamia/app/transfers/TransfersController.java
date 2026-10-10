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
package tools.dynamia.app.transfers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.TransferMeta;
import tools.dynamia.ui.files.TransferRef;
import tools.dynamia.ui.files.TransferStore;
import tools.dynamia.ui.files.TransferTooLargeException;
import tools.dynamia.ui.files.UploadDefaults;
import tools.dynamia.ui.files.UploadRejectedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Moves the files of remote clients in and out of the {@link TransferStore} by streaming.
 * <ul>
 *     <li>{@code POST /api/app/transfers}: raw {@code application/octet-stream} body, with {@code X-File-Name}
 *     (URL-encoded) and {@code X-File-Type}. It does not go through Spring multipart, so the multipart limits do not
 *     apply; {@code dynamia.ui.files.max-file-size} does, and a bigger file gets {@code 413}.</li>
 *     <li>{@code GET /api/app/transfers/{ref}}: streams the file, only to its owner; downloads are deleted after the
 *     first complete read.</li>
 *     <li>{@code DELETE /api/app/transfers/{ref}}: the client cancels an upload.</li>
 * </ul>
 * A file of another user or tenant is reported as not found, never as forbidden. The accepted types and sizes of each
 * upload are enforced again when the action consumes the reference.
 */
@RestController
@RequestMapping(TransfersController.PATH)
public class TransfersController {

    /** Base path of the endpoints. */
    public static final String PATH = "/api/app/transfers";

    private final TransferStore store;

    /**
     * @param store where the files are kept
     */
    public TransfersController(TransferStore store) {
        this.store = store;
    }

    /**
     * Receives one file.
     *
     * @param request the request; its body is the file
     * @return {@code {ref, name, contentType, size}}
     * @throws IOException when the body cannot be read
     */
    @PostMapping(consumes = "*/*", produces = "application/json")
    public ResponseEntity<Map<String, Object>> upload(HttpServletRequest request) throws IOException {
        String name = decode(request.getHeader("X-File-Name"));
        String type = request.getHeader("X-File-Type");
        if (type == null || type.isBlank()) {
            type = request.getContentType();
        }
        long max = UploadDefaults.maxFileSize();
        long declared = request.getContentLengthLong();
        if (declared > max) {
            throw new TransferTooLargeException(max);
        }
        TransferRef ref;
        try (InputStream in = request.getInputStream()) {
            ref = store.put(in, TransferMeta.of(name, type, TransferMeta.Direction.UPLOAD, FlowPrincipal.current()), max);
        }
        return ResponseEntity.ok(describe(ref));
    }

    /**
     * Sends a file to its owner.
     *
     * @param ref      the reference id
     * @param response the response, written by streaming
     * @throws IOException when writing fails
     */
    @GetMapping("/{ref}")
    public void download(@PathVariable("ref") String ref, HttpServletResponse response) throws IOException {
        var found = store.get(ref, FlowPrincipal.current());
        if (found.isEmpty()) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }
        var stored = found.get();
        response.setContentType(stored.ref().contentType() == null ? "application/octet-stream" : stored.ref().contentType());
        response.setContentLengthLong(stored.ref().size());
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encode(stored.ref().name()));
        response.setHeader("Cache-Control", "no-store");
        try (InputStream in = stored.openStream(); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
        if (stored.meta().direction() == TransferMeta.Direction.DOWNLOAD) {
            store.delete(ref);
        }
    }

    /**
     * Cancels an upload.
     *
     * @param ref the reference id
     * @return 204, also when it did not exist or is not the caller's
     */
    @DeleteMapping("/{ref}")
    public ResponseEntity<Void> cancel(@PathVariable("ref") String ref) {
        store.get(ref, FlowPrincipal.current()).ifPresent(found -> store.delete(ref));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(TransferTooLargeException.class)
    ResponseEntity<Map<String, Object>> tooLarge(TransferTooLargeException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", "TOO_LARGE", "message", e.getMessage()));
    }

    @ExceptionHandler(UploadRejectedException.class)
    ResponseEntity<Map<String, Object>> rejected(UploadRejectedException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", "REJECTED", "message", e.getMessage()));
    }

    private static Map<String, Object> describe(TransferRef ref) {
        var body = new LinkedHashMap<String, Object>();
        body.put("ref", ref.ref());
        body.put("name", ref.name());
        body.put("contentType", ref.contentType());
        body.put("size", ref.size());
        return body;
    }

    private static String decode(String header) {
        return header == null ? "file" : URLDecoder.decode(header, StandardCharsets.UTF_8);
    }

    private static String encode(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
