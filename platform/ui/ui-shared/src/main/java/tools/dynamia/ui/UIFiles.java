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

import tools.dynamia.ui.files.DownloadSource;
import tools.dynamia.ui.files.UploadOptions;
import tools.dynamia.ui.files.UploadedFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Facade for moving files between an action and the user, whatever front end the user is on. Same idea as
 * {@link UIMessages}: the action calls this class, and the {@link FileTransfer} of the environment does the work (see
 * {@link UIFacades}).
 *
 * <pre>{@code
 * UIFiles.download("report.json", "application/json", bytes);
 *
 * UIFiles.uploadOne(".json", file -> importer.importReport(file.openStream()));
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
     * Gives a small file to the user from bytes in memory. Use the {@link Path} or {@link Supplier} overloads for big ones.
     */
    public static void download(String fileName, String contentType, byte[] content) {
        download(new DownloadSource.BytesSource(fileName, contentType, content));
    }

    /**
     * Gives a file to the user by streaming it. The file is not deleted.
     */
    public static void download(Path file, String contentType) {
        download(new DownloadSource.PathSource(file.getFileName().toString(), contentType, file));
    }

    /**
     * Gives a file to the user by streaming it. The file is not deleted.
     */
    public static void download(File file, String contentType) {
        download(file.toPath(), contentType);
    }

    /**
     * Gives a file to the user from content produced on demand; {@code opener} is called when the user downloads it.
     */
    public static void download(String fileName, String contentType, Supplier<InputStream> opener) {
        download(new DownloadSource.StreamSource(fileName, contentType, -1, opener));
    }

    /**
     * Gives a file to the user.
     */
    public static void download(DownloadSource source) {
        UIFacades.port(FileTransfer.class).download(source);
    }

    /**
     * Shows a file to the user instead of saving it. Only PDF, common images and plain text are shown inline (see
     * {@link tools.dynamia.ui.files.InlinePolicy}); any other type is downloaded. In a remote run the file reaches the
     * client in the final response, like a download.
     *
     * @param source where the content comes from
     */
    public static void preview(DownloadSource source) {
        UIFacades.port(FileTransfer.class).preview(source);
    }

    /**
     * Shows in-memory content to the user.
     *
     * @param fileName    name shown to the user
     * @param contentType MIME type, for example {@code application/pdf}
     * @param content     the bytes
     */
    public static void preview(String fileName, String contentType, byte[] content) {
        preview(new DownloadSource.BytesSource(fileName, contentType, content));
    }

    /**
     * Shows a file of the file system to the user.
     *
     * @param file        the file
     * @param contentType MIME type, for example {@code application/pdf}
     */
    public static void preview(Path file, String contentType) {
        preview(new DownloadSource.PathSource(file.getFileName().toString(), contentType, file));
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
