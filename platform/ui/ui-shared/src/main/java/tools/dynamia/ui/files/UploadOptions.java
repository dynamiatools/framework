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

/**
 * What {@code UIFiles.upload} asks the user, and the limits the server enforces on what comes back.
 *
 * @param title        title of the dialog, may be null
 * @param accept       comma separated extensions and MIME types ({@code .xlsx,image/*}), may be null for anything
 * @param maxFiles     maximum number of files
 * @param maxFileSize  maximum size of each file in bytes
 * @param maxTotalSize maximum size of all files together in bytes, or 0 for no extra limit
 */
public record UploadOptions(String title, String accept, int maxFiles, long maxFileSize, long maxTotalSize) {

    /** @return an upload of one file of any type */
    public static UploadOptions single() {
        return single(null);
    }

    /**
     * @param accept accepted extensions and MIME types
     * @return an upload of one file
     */
    public static UploadOptions single(String accept) {
        return new UploadOptions(null, accept, 1, UploadDefaults.maxFileSize(), 0);
    }

    /**
     * @param accept accepted extensions and MIME types
     * @return an upload of several files
     */
    public static UploadOptions multiple(String accept) {
        return new UploadOptions(null, accept, UploadDefaults.maxFiles(), UploadDefaults.maxFileSize(), 0);
    }

    /** @return whether more than one file can be chosen */
    public boolean multiple() {
        return maxFiles > 1;
    }

    /**
     * @param title the new title
     * @return these options with that title
     */
    public UploadOptions title(String title) {
        return new UploadOptions(title, accept, maxFiles, maxFileSize, maxTotalSize);
    }
}
