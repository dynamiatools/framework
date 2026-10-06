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
package tools.dynamia.domain.query;

/**
 * Maps a persistent entity to another representation (for example a read-only {@link tools.dynamia.commons.BeanMap})
 * while the persistence context is still open. A mapper travels inside {@link QueryParameters} and is applied by the
 * {@link tools.dynamia.domain.services.CrudService} implementation to every query result, and by paginated results
 * (through {@link QueryMetadata}) every time a new page is loaded. It is {@link java.io.Serializable} because
 * {@link QueryParameters} is.
 *
 * @param <R> the mapped type
 */
@FunctionalInterface
public interface EntityMapper<R> extends java.io.Serializable {

    /**
     * Maps the given entity.
     *
     * @param entity the entity, never null
     * @return the mapped object
     */
    R map(Object entity);
}
