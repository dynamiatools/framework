
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

package tools.dynamia.modules.entityfile.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.io.IOUtils;
import tools.dynamia.modules.entityfile.*;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.domain.enums.EntityFileState;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.local.LocalEntityFileStorage;
import tools.dynamia.modules.entityfile.service.EntityFileService;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Mario Serrano Leones
 */
@Service
@CacheConfig(cacheNames = "entity-files")
public class EntityFileServiceImpl implements EntityFileService {

    @Autowired
    private Parameters appParams;

    private static final String DEFAULT_STORAGE = "DEFAULT_STORAGE_ID";

    private LoggingService logger = new SLF4JLoggingService(EntityFileService.class);

    @Autowired
    private CrudService crudService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public EntityFile createDirectory(EntityFile parent, String name, String description) {
        return createDir(parent, null, name, description);
    }

    @Override
    public EntityFile createDirectory(Object entity, String name, String description) {
        return createDir(null, entity, name, description);
    }

    private EntityFile createDir(EntityFile parent, Object targetEntity, String name, String descripcion) {
        EntityFile entityFile = new EntityFile();
        entityFile.setParent(parent);
        entityFile.setName(name);
        entityFile.setState(EntityFileState.VALID);
        entityFile.setDescription(descripcion);
        if (targetEntity != null) {
            configureEntityFile(targetEntity, entityFile);
        } else if (parent != null) {
            entityFile.setTargetEntity(parent.getTargetEntity());
            entityFile.setTargetEntityId(parent.getTargetEntityId());
            entityFile.setTargetEntitySId(parent.getTargetEntitySId());
        }
        entityFile.setShared(true);
        entityFile.setType(EntityFileType.DIRECTORY);
        entityFile = crudService.save(entityFile);
        return entityFile;
    }

    @Override
    @Transactional
    public EntityFile createEntityFile(UploadedFileInfo fileInfo, Object target, String description) {
        logger.info("Creating new entity file for " + (target != null ? target : "temporal entity") + ", file: " + fileInfo.getFullName());
        EntityFile entityFile = new EntityFile();
        entityFile.setDescription(description);
        entityFile.setContentType(fileInfo.getContentType());
        entityFile.setName(fileInfo.getFullName());
        entityFile.setExtension(StringUtils.getFilenameExtension(fileInfo.getFullName()));
        entityFile.setShared(fileInfo.isShared());
        entityFile.setSubfolder(fileInfo.getSubfolder());
        entityFile.setStoredFileName(fileInfo.getStoredFileName());
        entityFile.setExternalRef(fileInfo.getExternalRef());
        entityFile.setSize(fileInfo.getLength());

        configureEntityFile(target, entityFile);
        if (fileInfo.getAccountId() != null) {
            entityFile.setAccountId(fileInfo.getAccountId());
        } else {
            configureEntityFileAccount(entityFile);
        }
        entityFile.setType(EntityFileType.getFileType(entityFile.getExtension()));
        entityFile.setParent(fileInfo.getParent());
        entityFile.setState(EntityFileState.VALID);

        EntityFileStorage storage = getCurrentStorage();
        storage.upload(entityFile, fileInfo);
        entityFile.setStorageInfo(storage.getId());

        crudService.create(entityFile);
        syncEntityFileAware(target);

        return entityFile;
    }

    @Override
    public void configureEntityFile(Object target, EntityFile entityFile) {
        if (target != null) {
            if (DomainUtils.isEntity(target)) {
                entityFile.setTargetEntity(target.getClass().getName());
                Serializable id = DomainUtils.findEntityId(target);

                if (id == null) {
                    throw new EntityFileException("Null id for entity " + target.getClass() + " -> " + target);
                }

                if (id instanceof Long) {
                    entityFile.setTargetEntityId((Long) id);
                } else {
                    entityFile.setTargetEntitySId(id.toString());
                }
            } else {
                throw new EntityFileException("Target entity " + target.getClass() + " -> " + target + " is not a JPA Entity");
            }
        } else {
            entityFile.setTargetEntity("temporal");
        }
    }

    @Override
    @Transactional
    public EntityFile createEntityFile(UploadedFileInfo fileInfo, Object target) {
        return createEntityFile(fileInfo, target, null);
    }

    @Override
    @Transactional
    public EntityFile createTemporalEntityFile(UploadedFileInfo fileInfo) {
        EntityFile temp = createEntityFile(fileInfo, null);
        temp.setTargetEntity("temporal");
        temp.setTargetEntityId(System.currentTimeMillis());

        return temp;
    }

    @Override
    @Transactional
    public void delete(EntityFile entityFile) {
        EntityFileStorage storage = findStorage(entityFile.getStorageInfo());
        if (storage == null) {
            storage = getCurrentStorage();
        }

        storage.delete(entityFile);
    }

    @Override
    public List<EntityFile> getEntityFiles(Object entity) {
        if (entity != null) {
            return getEntityFiles(entity.getClass(), DomainUtils.findEntityId(entity), null);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<EntityFile> getEntityFiles(Object entity, EntityFile parentDirectory) {
        return getEntityFiles(entity.getClass(), DomainUtils.findEntityId(entity), parentDirectory);
    }

    @Override
    public List<EntityFile> getEntityFiles(Class clazz, Serializable id, EntityFile parentDirectory) {
        QueryParameters params = new QueryParameters();
        params.add("targetEntity", QueryConditions.eq(clazz.getName()));

        if (id instanceof Long) {
            params.add("targetEntityId", QueryConditions.eq(id));
        } else {
            params.add("targetEntitySId", QueryConditions.eq(id.toString()));
        }
        params.add("state", QueryConditions.notEq(EntityFileState.DELETED));
        if (parentDirectory == null) {
            params.add("parent", QueryConditions.isNull());
        } else {
            params.add("parent", parentDirectory);
        }
        params.orderBy("type", true);
        return crudService.find(EntityFile.class, params);
    }

    private long counttEntityFiles(Class clazz, Serializable id) {
        QueryParameters params = new QueryParameters();
        params.setAutocreateSearcheableStrings(false);
        params.add("targetEntity", QueryConditions.eq(clazz.getName()));
        if (id instanceof Long) {
            params.add("targetEntityId", QueryConditions.eq(id));
        } else {
            params.add("targetEntitySId", QueryConditions.eq(id.toString()));
        }
        params.add("state", EntityFileState.VALID);
        params.add("type", QueryConditions.in(EntityFileType.FILE, EntityFileType.IMAGE));

        return crudService.count(EntityFile.class, params);
    }

    @Override
    public void syncEntityFileAware() {
        List<String> targetEntities = crudService.getPropertyValues(EntityFile.class, "targetEntity");
        if (targetEntities != null) {
            logger.info("Syncing EntityFileAware entities");
            for (final String entityClassName : targetEntities) {
                if (!entityClassName.equals("temporal")) {
                    try {
                        Object object = BeanUtils.newInstance(entityClassName);
                        if (object instanceof EntityFileAware) {
                            crudService.executeWithinTransaction(() -> {

                                logger.info("Processing batch EntityFileAware for " + entityClassName);
                                String updateQuery = "update "
                                        + entityClassName
                                        + " e set e.filesCount = (select count(ef.id) from EntityFile ef where ef.targetEntityId = e.id and ef.state = :state and ef.type in (:types) and ef.targetEntity='"
                                        + entityClassName + "')";
                                QueryParameters parameters = QueryParameters.with("state", EntityFileState.VALID)
                                        .add("types", Arrays.asList(EntityFileType.FILE, EntityFileType.IMAGE));
                                crudService.execute(updateQuery, parameters);

                            });
                        }
                    } catch (Exception e) {
                        logger.warn("Cannot sync EntityFile " + entityClassName + ". Error: " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public StoredEntityFile download(EntityFile file) {
        EntityFileStorage storage = null;
        if (file.getStorageInfo() != null && !file.getStorageInfo().isEmpty()) {
            storage = findStorage(file.getStorageInfo());
        }

        if (storage == null) {
            storage = getCurrentStorage();
        }

        return storage.download(file);

    }

    @Override
    public void download(EntityFile entityFile, File outputFile) {
        try {
            StoredEntityFile storedEntityFile = download(entityFile);
            if (storedEntityFile != null) {
                IOUtils.copy(new URL(storedEntityFile.getUrl()).openStream(), outputFile);
            }
        } catch (Exception e) {
            throw new EntityFileException("Error downloading entity file to local file", e);
        }
    }

    @Override
    @Cacheable
    public EntityFile getEntityFile(String uuid) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityFile> query = cb.createQuery(EntityFile.class);
            Root<EntityFile> root = query.from(EntityFile.class);
            query.select(root).where(cb.equal(root.get("uuid"), uuid));
            return entityManager.createQuery(query).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            logger.error("Error loading entity file with uuid: " + uuid + ".  " + e.getMessage(), e);

            return null;
        }

    }

    @Override
    @Transactional
    public void syncEntityFileAware(Object target) {
        if (target != null && target instanceof EntityFileAware) {
            logger.info("Processing EntityFileAware for " + target.getClass() + " - " + target);
            EntityFileAware efa = (EntityFileAware) target;
            efa.setFilesCount(counttEntityFiles(target.getClass(), DomainUtils.findEntityId(target)));
            crudService.update(target);
        }
    }

    private EntityFileStorage getCurrentStorage() {
        String storageId = appParams.getValue(DEFAULT_STORAGE, LocalEntityFileStorage.ID);
        EntityFileStorage storage = findStorage(storageId);
        if (storage == null) {
            throw new EntityFileException("No default " + EntityFileStorage.class
                    .getSimpleName() + " configured");
        }
        return storage;
    }

    private EntityFileStorage findStorage(String storageId) {
        Optional<EntityFileStorage> storage = Containers.get().findObjects(EntityFileStorage.class)
                .stream()
                .filter(s -> s.getId().equals(storageId))
                .findFirst();

        if (storage.isPresent()) {
            return storage.get();
        } else {
            return null;
        }
    }


    private void fixuuid() {
        crudService.executeWithinTransaction(() -> {

            logger.info("Fixing null UUIDs");
            String updateQuery = "update " + EntityFile.class
                    .getName() + " e set e.uuid = e.id where e.uuid is null";
            crudService.execute(updateQuery, new QueryParameters());

        });

    }

    private void configureEntityFileAccount(EntityFile entityFile) {
        EntityFileAccountProvider provider = Containers.get().findObject(EntityFileAccountProvider.class);
        if (provider != null) {
            entityFile.setAccountId(provider.getAccountId());
        }
    }

}
