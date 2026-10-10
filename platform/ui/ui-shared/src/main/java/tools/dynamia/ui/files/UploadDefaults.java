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
 * Default limits of {@link UploadOptions}. The application sets them at startup from the {@code dynamia.ui.files.*}
 * properties; before that, or without an application, these built-in values apply (100 MB per file, 10 files).
 */
public final class UploadDefaults {

    /** Built-in maximum size of a file: 100 MB, as the ERP's ZK {@code max-upload-size}. */
    public static final long BUILT_IN_MAX_FILE_SIZE = 100L * 1024 * 1024;
    /** Built-in maximum number of files of one upload. */
    public static final int BUILT_IN_MAX_FILES = 10;

    private static volatile long maxFileSize = BUILT_IN_MAX_FILE_SIZE;
    private static volatile int maxFiles = BUILT_IN_MAX_FILES;

    private UploadDefaults() {
    }

    /** @return the maximum size of one file when an upload does not say */
    public static long maxFileSize() {
        return maxFileSize;
    }

    /** @return the maximum number of files when an upload does not say */
    public static int maxFiles() {
        return maxFiles;
    }

    /**
     * @param maxFileSize maximum bytes of one file
     * @param maxFiles    maximum files of one upload
     */
    public static void set(long maxFileSize, int maxFiles) {
        UploadDefaults.maxFileSize = maxFileSize;
        UploadDefaults.maxFiles = maxFiles;
    }
}
