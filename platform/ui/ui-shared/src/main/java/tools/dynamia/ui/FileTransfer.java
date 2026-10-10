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

import java.util.List;
import java.util.function.Consumer;

/**
 * SPI behind {@link UIFiles}: moves files between the application and the user. ZK implements it with
 * {@code Filedownload}/{@code Fileupload}; a headless run answers the upload from the client and hands the downloads
 * to it with the response.
 */
@UIPort(name = "files", steps = {"UPLOAD"})
public interface FileTransfer {

    /**
     * Gives a file to the user. The content is read when the user downloads it, never fully into memory unless the source
     * is a {@link DownloadSource.BytesSource}.
     *
     * @param source where the content comes from
     */
    void download(DownloadSource source);

    /**
     * Asks the user for files. {@code onFiles} runs when the user has chosen them (never when they cancel). The limits of {@code options} are enforced on the server.
     *
     * @param options what to ask
     * @param onFiles receives the chosen files
     */
    void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles);
}
