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
import tools.dynamia.ui.UploadOptions;
import tools.dynamia.ui.UploadedFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ZK implementation of {@link FileTransfer}: the browser downloads with {@link Filedownload} and uploads with the
 * {@link Fileupload} dialog.
 */
public class ZKFileTransfer implements FileTransfer {

    @Override
    public void download(String fileName, String contentType, byte[] content) {
        Filedownload.save(content, contentType, fileName);
    }

    @Override
    public void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
        int max = options.multiple() ? 10 : 1;
        Fileupload.get(max, event -> {
            var files = new ArrayList<UploadedFile>();
            for (Media media : event.getMedias()) {
                files.add(toUploadedFile(media));
            }
            if (!files.isEmpty()) {
                onFiles.accept(files);
            }
        });
    }

    private static UploadedFile toUploadedFile(Media media) {
        try {
            return new UploadedFile(media.getName(), media.getContentType(), media.getStreamData().readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
