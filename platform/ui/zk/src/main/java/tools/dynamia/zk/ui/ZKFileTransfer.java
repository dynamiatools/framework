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
package tools.dynamia.zk.ui;

import org.zkoss.util.media.Media;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.files.DownloadSource;
import tools.dynamia.ui.files.UploadOptions;
import tools.dynamia.ui.files.UploadPolicy;
import tools.dynamia.ui.files.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ZK implementation of {@link FileTransfer}: the browser downloads with {@link Filedownload} and uploads with the
 * {@link Fileupload} dialog.
 * <p>
 * Nothing is read fully into memory: downloads hand ZK a stream or a file, and each uploaded {@link Media} is copied by
 * streaming to a temporary file wrapped as an {@link UploadedFile}. Text media are decoded by ZK and stored as UTF-8. The
 * limits of the {@link UploadOptions} are checked here, on the server, with the same {@link UploadPolicy} as the remote
 * adapter.
 */
public class ZKFileTransfer implements FileTransfer {

    @Override
    public void download(DownloadSource source) {
        switch (source) {
            case DownloadSource.BytesSource bytes -> Filedownload.save(bytes.content(), bytes.contentType(), bytes.name());
            case DownloadSource.PathSource path -> Filedownload.save(path.openStream(), path.contentType(), path.name());
            case DownloadSource.StreamSource stream -> Filedownload.save(stream.openStream(), stream.contentType(), stream.name());
        }
    }

    @Override
    public void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
        Fileupload.get(options.maxFiles(), event -> {
            var files = new ArrayList<UploadedFile>();
            for (Media media : event.getMedias()) {
                files.add(wrap(media));
            }
            UploadPolicy.check(options, files);
            if (!files.isEmpty()) {
                onFiles.accept(files);
            }
        });
    }

    /**
     * Copies {@code media} by streaming to a temporary file and wraps it.
     *
     * @param media what ZK received
     * @return the handle; the temporary file is deleted when the JVM exits
     */
    static UploadedFile wrap(Media media) {
        try {
            String extension = extensionOf(media.getName());
            Path file = Files.createTempFile("zk-upload", extension.isEmpty() ? ".tmp" : "." + extension);
            file.toFile().deleteOnExit();
            if (media.isBinary()) {
                try (InputStream in = media.getStreamData()) {
                    Files.copy(in, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // ZK decodes text media with the charset it received; the copy is stored as UTF-8
                try (Reader in = media.getReaderData(); var out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    in.transferTo(out);
                }
            }
            return UploadedFile.of(media.getName(), media.getContentType(), file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String extensionOf(String name) {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).replaceAll("[^A-Za-z0-9]", "");
    }
}
