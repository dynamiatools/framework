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
package tools.dynamia.ui.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file the user gave to an action. It is a handle, not the bytes: the content is read by streaming, so a 100 MB import
 * does not have to fit in memory, in a token or in a JSON document.
 * <pre>{@code
 * UIFiles.uploadOne(".xlsx", file -> importer.importFrom(file.openStream()));
 * }</pre>
 */
public interface UploadedFile {

    /** @return the file name the user chose */
    String name();

    /** @return the MIME type, may be {@code null} */
    String contentType();

    /** @return the size in bytes */
    long size();

    /**
     * Opens the content. It can be opened several times while the handle is alive; the caller closes each stream.
     *
     * @return a new stream over the content
     */
    InputStream openStream();

    /**
     * Copies the content, by streaming, to a temporary file for APIs that need a {@link Path}. The caller deletes it.
     *
     * @return the temporary file
     */
    default Path toTempFile() {
        try (InputStream in = openStream()) {
            String extension = extension();
            Path file = Files.createTempFile("upload", extension.isEmpty() ? ".tmp" : "." + extension);
            Files.copy(in, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** @return the extension of the name in lower case, without the dot, or an empty string */
    default String extension() {
        String name = name();
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
    }

    /**
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param content     the bytes
     * @return a handle over bytes already in memory; for small files and tests
     */
    static UploadedFile of(String name, String contentType, byte[] content) {
        return new BytesUploadedFile(name, contentType, content);
    }

    /**
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param file        a file on disk
     * @return a handle over a file; it does not delete it
     */
    static UploadedFile of(String name, String contentType, Path file) {
        return new PathUploadedFile(name, contentType, file);
    }
}
