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
package tools.dynamia.crud.actions.remote;

import tools.dynamia.domain.services.CrudService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared logic for {@link DeleteRemoteAction} and {@link DeleteFlowRemoteAction}: resolves either a single
 * entity id ({@code dataId}) or a bulk list ({@code data: {"ids": [...]}}, or a raw list body) into one
 * uniform id list, and deletes them — same class handles single and bulk delete, no separate
 * {@code BulkDeleteRemoteAction}. See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.2.
 *
 * @author Mario A. Serrano Leones
 */
final class DeleteSupport {

    private DeleteSupport() {
    }

    /**
     * Resolves the ids to delete from a request/flow payload: {@code dataId} for a single entity, or
     * {@code data}'s {@code "ids"} key (or {@code data} itself, when it's already a list) for bulk delete.
     *
     * @throws IllegalArgumentException if neither shape is present
     */
    @SuppressWarnings("unchecked")
    static List<Serializable> resolveIds(String dataId, Object data) {
        if (dataId != null && !dataId.isBlank()) {
            return List.of(dataId);
        }
        if (data instanceof Map<?, ?> map && map.get("ids") instanceof List<?> ids) {
            return (List<Serializable>) ids;
        }
        if (data instanceof List<?> ids) {
            return (List<Serializable>) ids;
        }
        throw new IllegalArgumentException("Delete action requires \"dataId\" or a list of ids");
    }

    /** Deletes every id found, skipping (rather than failing on) ids that no longer exist. */
    static List<Object> delete(CrudService crudService, Class<?> entityClass, List<Serializable> ids) {
        List<Object> deleted = new ArrayList<>();
        for (Serializable id : ids) {
            Object entity = crudService.find(entityClass, id);
            if (entity != null) {
                crudService.delete(entityClass, id);
                deleted.add(entity);
            }
        }
        return deleted;
    }
}
