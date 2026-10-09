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
package tools.dynamia.actions.replay;

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.UploadOptions;
import tools.dynamia.ui.UploadedFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link FileTransfer} of a headless run. A download is recorded in the {@link ReplaySession} and travels with the
 * response (Base64 in {@link ReplayExecutor#DOWNLOADS_PARAM}); an upload is an {@code UPLOAD} step the client answers
 * with the files (Base64).
 * <p>
 * Files travel inline, so both directions are capped: this is for documents (an exported report, a JSON or Excel file to
 * import), not for large media. A stream endpoint for big files is a separate piece of work.
 */
public final class ReplayFileTransfer implements FileTransfer {

    /** Biggest file the action can give to the user in one response. */
    public static final int MAX_DOWNLOAD_BYTES = 10 * 1024 * 1024;

    /** Biggest file the user can give to the action. It also travels inside the resume token of later questions. */
    public static final int MAX_UPLOAD_BYTES = 1024 * 1024;

    private final ReplaySession session;

    public ReplayFileTransfer(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void download(String fileName, String contentType, byte[] content) {
        if (content.length > MAX_DOWNLOAD_BYTES) {
            throw new IllegalArgumentException("File " + fileName + " is too big to download from an action ("
                    + content.length + " bytes, limit " + MAX_DOWNLOAD_BYTES + ")");
        }
        session.download(fileName, contentType, content);
    }

    @Override
    public void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
        session.interact(ActionFlowStep.upload(options.title(), options.accept(), options.multiple()), answer -> {
            var files = toFiles(answer);
            if (!files.isEmpty()) {
                onFiles.accept(files);
            }
        });
    }

    private static List<UploadedFile> toFiles(Object answer) {
        var files = new ArrayList<UploadedFile>();
        if (answer instanceof Map<?, ?> single) {
            files.add(toFile(single));
        } else if (answer instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    files.add(toFile(map));
                }
            }
        }
        return files;
    }

    private static UploadedFile toFile(Map<?, ?> map) {
        var name = String.valueOf(map.get("name"));
        var content = map.get("content") == null ? new byte[0] : Base64.getDecoder().decode(String.valueOf(map.get("content")));
        if (content.length > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("File " + name + " is too big to upload to an action (" + content.length
                    + " bytes, limit " + MAX_UPLOAD_BYTES + ")");
        }
        var type = map.get("contentType");
        return new UploadedFile(name, type == null ? null : String.valueOf(type), content);
    }
}
