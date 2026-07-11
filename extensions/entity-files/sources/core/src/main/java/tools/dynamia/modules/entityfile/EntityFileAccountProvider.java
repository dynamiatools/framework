
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
 * Resolves tenant/account context for entity-file operations.
 * Implementations provide the current account id and can validate whether
 * a specific {@link EntityFile} belongs to a valid account scope.
 */
public interface EntityFileAccountProvider {

    /**
     * Returns the current tenant account identifier.
     *
     * @return the current account id
     */
    Long getAccountId();


    /**
     * Validates whether the given entity file has a usable account id.
     * The default implementation checks that the entity file is not null,
     * account id is not null, and account id is greater than zero.
     *
     * @param entityFile the entity file to validate
     * @return {@code true} when the file has a valid account id
     */
    default boolean isValidEntityFile(EntityFile entityFile) {
        return entityFile != null && entityFile.getAccountId() != null && entityFile.getAccountId() > 0;
    }

}
