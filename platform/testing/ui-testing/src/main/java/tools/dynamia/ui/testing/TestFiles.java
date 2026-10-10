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
package tools.dynamia.ui.testing;

import tools.dynamia.ui.files.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds the files a scripted user uploads.
 *
 * <pre>{@code
 * u.upload(TestFiles.resource("/reports/sales.json"));
 * }</pre>
 */
public final class TestFiles {

    private TestFiles() {
    }

    /**
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param content     the bytes
     * @return the file
     */
    public static UploadedFile of(String name, String contentType, byte[] content) {
        return UploadedFile.of(name, contentType, content);
    }

    /**
     * @param name    file name
     * @param content UTF-8 text
     * @return the file, as {@code text/plain}
     */
    public static UploadedFile text(String name, String content) {
        return of(name, "text/plain", content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param file a file on disk
     * @return the file, named as on disk and read by streaming
     */
    public static UploadedFile of(Path file) {
        try {
            return UploadedFile.of(file.getFileName().toString(), Files.probeContentType(file), file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @param resource absolute classpath location, such as {@code /reports/sales.json}
     * @return the file, named after the last segment of the path
     */
    public static UploadedFile resource(String resource) {
        try (InputStream in = TestFiles.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resource);
            }
            return of(resource.substring(resource.lastIndexOf('/') + 1), null, in.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
