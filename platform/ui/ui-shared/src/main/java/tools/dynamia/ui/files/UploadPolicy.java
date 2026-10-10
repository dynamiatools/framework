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

import java.util.List;
import java.util.Locale;

/**
 * Checks what a user uploaded against the {@link UploadOptions} the action asked with. The same check runs in every
 * adapter, on the server, so a client that skips its own validation gets the same answer.
 */
public final class UploadPolicy {

    private UploadPolicy() {
    }

    /**
     * @param options what the action asked
     * @param files   what arrived
     * @throws UploadRejectedException when a limit or the accepted types are broken
     */
    public static void check(UploadOptions options, List<? extends UploadedFile> files) {
        if (files.size() > options.maxFiles()) {
            throw new UploadRejectedException("At most " + options.maxFiles() + " file(s) are allowed, got " + files.size());
        }
        long total = 0;
        for (UploadedFile file : files) {
            checkFile(options, file.name(), file.contentType(), file.size());
            total += file.size();
        }
        if (options.maxTotalSize() > 0 && total > options.maxTotalSize()) {
            throw new UploadRejectedException("The files add up to " + total + " bytes, the limit is " + options.maxTotalSize());
        }
    }

    /**
     * Checks one file before it is stored.
     *
     * @param options     what the action asked
     * @param name        file name
     * @param contentType MIME type, may be null
     * @param size        size in bytes
     * @throws UploadRejectedException when the file breaks the limits or the accepted types
     */
    public static void checkFile(UploadOptions options, String name, String contentType, long size) {
        if (size > options.maxFileSize()) {
            throw new UploadRejectedException("File " + name + " is " + size + " bytes, the limit is " + options.maxFileSize());
        }
        if (!accepts(options.accept(), name, contentType)) {
            throw new UploadRejectedException("File " + name + " is not of an accepted type: " + options.accept());
        }
    }

    /**
     * @param accept      comma separated extensions ({@code .json}), MIME types ({@code image/png}) and wildcards
     *                    ({@code image/*}); null or blank accepts anything
     * @param name        file name
     * @param contentType MIME type, may be null
     * @return whether the file matches
     */
    public static boolean accepts(String accept, String name, String contentType) {
        if (accept == null || accept.isBlank()) {
            return true;
        }
        String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        String lowerType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        for (String rule : accept.split(",")) {
            String token = rule.trim().toLowerCase(Locale.ROOT);
            if (token.isEmpty()) {
                continue;
            }
            if (token.startsWith(".") && lowerName.endsWith(token)) {
                return true;
            }
            if (token.endsWith("/*") && lowerType.startsWith(token.substring(0, token.length() - 1))) {
                return true;
            }
            if (token.equals(lowerType)) {
                return true;
            }
        }
        return false;
    }
}
