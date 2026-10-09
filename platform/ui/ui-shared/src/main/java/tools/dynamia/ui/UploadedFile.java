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
package tools.dynamia.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A file the user gave to the application. It is held in memory, so it is meant for documents, not for large media.
 *
 * @param name        original file name
 * @param contentType MIME type, may be {@code null}
 * @param content     the bytes
 */
public record UploadedFile(String name, String contentType, byte[] content) {

    public InputStream stream() {
        return new ByteArrayInputStream(content);
    }

    public String extension() {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
    }
}
