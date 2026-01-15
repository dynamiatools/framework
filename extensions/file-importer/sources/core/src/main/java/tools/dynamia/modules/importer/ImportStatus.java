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

package tools.dynamia.modules.importer;

import tools.dynamia.domain.util.DomainUtils;

import java.io.Serializable;


public class ImportStatus implements Serializable {


    private long row;
    private boolean imported;
    private String message;
    private Serializable entityId;
    private String entityName;
    private Object entity;

    public ImportStatus(long row, boolean imported, String message, Object entity) {
        this.row = row;
        this.imported = imported;
        this.message = message;
        this.entity = entity;
        this.entityId = DomainUtils.findEntityId(entity);
        this.entityName = entity.toString();
    }

    public ImportStatus(long row, boolean imported, String message, Serializable entityId, String entityName) {
        this.row = row;
        this.imported = imported;
        this.message = message;
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public ImportStatus(long row, boolean imported, String message) {
        this.row = row;
        this.imported = imported;
        this.message = message;
    }

    public long getRow() {
        return row;
    }

    public boolean isImported() {
        return imported;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntity() {
        return entity;
    }
}
