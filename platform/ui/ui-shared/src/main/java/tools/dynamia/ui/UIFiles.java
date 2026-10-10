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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

/**
 * Facade for moving files between an action and the user, whatever front end the user is on. Same idea as
 * {@link UIMessages}: the action calls this class, and the {@link FileTransfer} of the environment does the work (see
 * {@link UIFacades}).
 *
 * <pre>{@code
 * UIFiles.download("report.json", "application/json", bytes);
 *
 * UIFiles.uploadOne(".json", file -> importer.importReport(file.stream()));
 * }</pre>
 * <p>
 * As with questions in {@link UIMessages}, the code that handles the uploaded files goes in the callback. In a headless
 * run the action is executed again once the client sent the files, so everything before the {@code upload} call must be
 * repeatable.
 */
public final class UIFiles {

    private UIFiles() {
    }

    /**
     * Gives a file to the user.
     */
    public static void download(String fileName, String contentType, byte[] content) {
        UIFacades.port(FileTransfer.class).download(fileName, contentType, content);
    }

    /**
     * Gives a file to the user, reading it fully. The file is not deleted.
     */
    public static void download(File file, String contentType) {
        try {
            download(file.getName(), contentType, Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Asks the user for files.
     */
    public static void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
        UIFacades.port(FileTransfer.class).upload(options, onFiles);
    }

    /**
     * Asks the user for several files of the given type(s).
     */
    public static void upload(String accept, Consumer<List<UploadedFile>> onFiles) {
        upload(UploadOptions.multiple(accept), onFiles);
    }

    /**
     * Asks the user for one file of the given type(s).
     */
    public static void uploadOne(String accept, Consumer<UploadedFile> onFile) {
        upload(UploadOptions.single(accept), files -> {
            if (!files.isEmpty()) {
                onFile.accept(files.get(0));
            }
        });
    }
}
