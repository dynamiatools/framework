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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalLong;
import java.util.function.Supplier;

/**
 * Where the content of a file given to the user comes from. Only {@link BytesSource} keeps bytes in memory; the others are
 * read when the user downloads, so there is no fixed size limit.
 */
public sealed interface DownloadSource permits DownloadSource.BytesSource, DownloadSource.PathSource, DownloadSource.StreamSource {

    /** @return the file name the user sees */
    String name();

    /** @return the MIME type, may be null */
    String contentType();

    /** @return the size when known */
    OptionalLong size();

    /**
     * @return a new stream over the content; the caller closes it
     */
    InputStream openStream();

    /**
     * Bytes already in memory: for small files.
     *
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param content     the bytes
     */
    record BytesSource(String name, String contentType, byte[] content) implements DownloadSource {
        @Override
        public OptionalLong size() {
            return OptionalLong.of(content.length);
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(content);
        }
    }

    /**
     * A file on disk. It is not deleted.
     *
     * @param name        file name the user sees
     * @param contentType MIME type, may be null
     * @param file        the file
     */
    record PathSource(String name, String contentType, Path file) implements DownloadSource {
        @Override
        public OptionalLong size() {
            try {
                return OptionalLong.of(Files.size(file));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public InputStream openStream() {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Content produced on demand.
     *
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param knownSize   the size in bytes, or a negative number when unknown
     * @param opener      opens a new stream each time it is called
     */
    record StreamSource(String name, String contentType, long knownSize, Supplier<InputStream> opener) implements DownloadSource {
        @Override
        public OptionalLong size() {
            return knownSize >= 0 ? OptionalLong.of(knownSize) : OptionalLong.empty();
        }

        @Override
        public InputStream openStream() {
            return opener.get();
        }
    }
}
