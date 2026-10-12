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
import java.util.Optional;

/**
 * Temporary storage of the files that travel between a remote client and an action: uploads wait here until the action
 * reads them, downloads wait until the user fetches them. The default implementation keeps them on the local disk; with
 * several nodes behind a balancer a shared implementation (S3, a shared file system) is needed.
 * <p>
 * Everything is streamed: no method needs the whole content in memory.
 */
public interface TransferStore {

    /**
     * Stores {@code data}. If it has more than {@code maxBytes}, reading stops, what was written is deleted and a
     * {@link TransferTooLargeException} is thrown.
     *
     * @param data     the content; the caller closes it
     * @param meta     whose file it is
     * @param maxBytes largest accepted size
     * @return the reference to the stored file
     * @throws TransferTooLargeException when it does not fit
     */
    TransferRef put(InputStream data, TransferMeta meta, long maxBytes);

    /**
     * @param id    the reference id
     * @param owner who asks
     * @return the file, or empty when it does not exist, expired, or belongs to someone else (a foreign file is never
     * revealed)
     */
    Optional<StoredTransfer> get(String id, FlowPrincipal owner);

    /**
     * @param id the reference id; unknown ids are ignored
     */
    void delete(String id);

    /** Deletes every expired file. */
    void purgeExpired();
}
