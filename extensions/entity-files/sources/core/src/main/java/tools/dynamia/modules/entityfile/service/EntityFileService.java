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

package tools.dynamia.modules.entityfile.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import tools.dynamia.modules.entityfile.StoredEntityFile;
import tools.dynamia.modules.entityfile.UploadedFileInfo;
import tools.dynamia.modules.entityfile.domain.EntityFile;

/**
 * The Interface EntityFileService.
 *
 * @author Mario Serrano
 */
public interface EntityFileService {

	/**
	 * Creates the directory.
	 *
	 * @param ownerEntity
	 *            the owner entity
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @return the entity file
	 */
    EntityFile createDirectory(Object ownerEntity, String name, String description);

	/**
	 * Creates the directory.
	 *
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @return the entity file
	 */
    EntityFile createDirectory(EntityFile parent, String name, String description);

	/**
	 * Creates the entity file.
	 *
	 * @param fileInfo
	 *            the file info
	 * @param target
	 *            the target
	 * @param desc
	 *            the desc
	 * @return the entity file
	 */
    EntityFile createEntityFile(UploadedFileInfo fileInfo, Object target, String desc);

	/**
	 * Creates the entity file.
	 *
	 * @param fileInfo
	 *            the file info
	 * @param targetEntity
	 *            the target entity
	 * @return the entity file
	 */
    EntityFile createEntityFile(UploadedFileInfo fileInfo, Object targetEntity);

	/**
	 * Gets the entity files.
	 *
	 * @param clazz
	 *            the clazz
	 * @param id
	 *            the id
	 * @param parentDirectory
	 *            the parent directory
	 * @return the entity files
	 */
    List<EntityFile> getEntityFiles(Class clazz, Serializable id, EntityFile parentDirectory);

	/**
	 * Gets the entity files.
	 *
	 * @param entity
	 *            the entity
	 * @param parentDirectory
	 *            the parent directory
	 * @return the entity files
	 */
    List<EntityFile> getEntityFiles(Object entity, EntityFile parentDirectory);

	/**
	 * Gets the entity files.
	 *
	 * @param entity
	 *            the entity
	 * @return the entity files
	 */
    List<EntityFile> getEntityFiles(Object entity);

	/**
	 * Delete.
	 *
	 * @param entityFile
	 *            the entity file
	 */
    void delete(EntityFile entityFile);

	/**
	 * Sync entity file aware.
	 */
    void syncEntityFileAware();

	/**
	 * Get an stored entity file instance for download
	 * @param entityFile
	 * @return
	 */
    StoredEntityFile download(EntityFile entityFile);

	/**
	 * Download the EntityFile internal file to a local output file, this is
	 * usefull when entityfiles are stored in difernte localtion
	 *
	 * @param entityFile
	 * @param outputFile
	 */
    void download(EntityFile entityFile, File outputFile);

	/**
	 * Creates the temporal entity file.
	 *
	 * @param fileInfo
	 *            the file info
	 * @return the entity file
	 */
    EntityFile createTemporalEntityFile(UploadedFileInfo fileInfo);

	/**
	 * Configure entity file. Setup targetEntity and targetEntityId for
	 * EntityFile
	 *
	 * @param target
	 *            the target
	 * @param entityFile
	 *            the entity file
	 */
    void configureEntityFile(Object target, EntityFile entityFile);

	void syncEntityFileAware(Object target);

	EntityFile getEntityFile(String uuid);

}
