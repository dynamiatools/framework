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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file the action gave to the user, kept for the test to inspect.
 *
 * @param name        file name
 * @param contentType MIME type, may be null
 * @param content     the bytes
 */
public record CapturedDownload(String name, String contentType, byte[] content) {

    /** @return the size in bytes */
    public long size() {
        return content.length;
    }

    /** @return a new stream over the content */
    public InputStream openStream() {
        return new ByteArrayInputStream(content);
    }

    /** @return the content as UTF-8 text */
    public String asString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * Writes the content to {@code target}.
     *
     * @param target destination file
     */
    public void saveTo(Path target) {
        try {
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
