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

package tools.dynamia.modules.entityfile;

import tools.dynamia.modules.entityfile.domain.EntityFile;

/**
 * Defines the contract for storing and retrieving files associated with domain entities.
 * Implementations may use local storage, cloud providers, or any custom backend.
 */
public interface EntityFileStorage {

    /**
     * Returns the unique technical identifier of this storage implementation.
     *
     * @return the storage identifier, used internally to select the implementation
     */
    String getId();

    /**
     * Returns the human-readable name of this storage implementation.
     *
     * @return display name for logs, configuration, or UI labels
     */
    String getName();

    /**
     * Uploads and persists the provided file information for the given entity file record.
     *
     * @param entityFile the entity file metadata to be updated with storage information
     * @param fileInfo metadata and stream details of the file to upload
     */
    void upload(EntityFile entityFile, UploadedFileInfo fileInfo);

    /**
     * Downloads the previously stored file associated with the given entity file record.
     *
     * @param entityFile the entity file metadata used to locate the stored file
     * @return the stored file content and metadata
     */
    StoredEntityFile download(EntityFile entityFile);

    /**
     * Deletes the stored file associated with the given entity file record.
     *
     * @param entityFile the entity file metadata used to locate and remove the stored file
     */
    void delete(EntityFile entityFile);

    /**
     * Reloads storage-specific runtime parameters.
     * <p>
     * Implementations can override this method when they need to refresh dynamic
     * settings (for example, credentials, endpoints, or bucket names) without restart.
     */
    default void reloadParams(){

    }

}
