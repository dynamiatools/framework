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

import java.io.InputStream;
import java.time.Instant;

/**
 * A file held by a {@link TransferStore}.
 */
public interface StoredTransfer {

    /** @return the reference of the file */
    TransferRef ref();

    /** @return who it belongs to and which way it goes */
    TransferMeta meta();

    /** @return when the store discards it */
    Instant expiresAt();

    /** @return a new stream over the content; the caller closes it */
    InputStream openStream();

    /** @return an {@link UploadedFile} handle over this file */
    default UploadedFile asUploadedFile() {
        TransferRef ref = ref();
        return new UploadedFile() {
            @Override
            public String name() {
                return ref.name();
            }

            @Override
            public String contentType() {
                return ref.contentType();
            }

            @Override
            public long size() {
                return ref.size();
            }

            @Override
            public InputStream openStream() {
                return StoredTransfer.this.openStream();
            }
        };
    }
}
